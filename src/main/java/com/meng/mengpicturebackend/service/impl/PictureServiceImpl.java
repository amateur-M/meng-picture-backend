package com.meng.mengpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meng.mengpicturebackend.exception.BusinessException;
import com.meng.mengpicturebackend.exception.ErrorCode;
import com.meng.mengpicturebackend.exception.ThrowUtils;
import com.meng.mengpicturebackend.manager.upload.FilePictureUpload;
import com.meng.mengpicturebackend.manager.upload.PictureUploadTemplate;
import com.meng.mengpicturebackend.manager.upload.UrlPictureUpload;
import com.meng.mengpicturebackend.mapper.PictureMapper;
import com.meng.mengpicturebackend.model.dto.file.UploadPictureResult;
import com.meng.mengpicturebackend.model.dto.picture.PictureQueryRequest;
import com.meng.mengpicturebackend.model.dto.picture.PictureReviewRequest;
import com.meng.mengpicturebackend.model.dto.picture.PictureUploadByBatchRequest;
import com.meng.mengpicturebackend.model.dto.picture.PictureUploadRequest;
import com.meng.mengpicturebackend.model.entity.Picture;
import com.meng.mengpicturebackend.model.entity.User;
import com.meng.mengpicturebackend.model.enums.PictureReviewStatusEnum;
import com.meng.mengpicturebackend.model.vo.PictureVO;
import com.meng.mengpicturebackend.model.vo.UserVO;
import com.meng.mengpicturebackend.service.PictureService;
import com.meng.mengpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author menglingqi
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2025-02-18 14:17:38
*/
@Slf4j
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService{

    @Resource
    UserService userService;

    @Resource
    UrlPictureUpload urlPictureUpload;

    @Resource
    FilePictureUpload filePictureUpload;

    /**
     * @description:  上传图片 --> 数据万象解析 --> 填充其他信息 --> 数据库入库
     * @param[1] inputSource
     * @param[2] pictureUploadRequest
     * @param[3] loginUser
     * @throws:
     * @return:
     */
    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        // 用于判断是更新还是上传
        Long pictureId = null;
        if (pictureUploadRequest != null)
            pictureId = pictureUploadRequest.getId();

        // 如果更新图片，校验参数是否存在
        if (pictureId != null && pictureId > 0) {
            // 查询数据库
            Picture oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            // 仅本人或者管理员可编辑图片
            if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser))
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有权限编辑");

            // boolean exists = this.lambdaQuery().eq(Picture::getId, pictureId).exists();
            // ThrowUtils.throwIf(!exists, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        }
        // 上传图片得到信息
        // 根据用户划分目录
        String filePrefix = String.format("public/%s", loginUser.getId());

        // 默认本地文件上传
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        if (inputSource instanceof String)
            pictureUploadTemplate = urlPictureUpload;

        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, filePrefix);
        // 构造入库信息
        Picture picture = new Picture();

        picture.setUrl(uploadPictureResult.getUrl());
        // 默认从解析的url中获取，支持自定义
        String picName = uploadPictureResult.getPicName();
        if (pictureUploadRequest != null && StrUtil.isNotBlank(pictureUploadRequest.getPicName()))
            picName = pictureUploadRequest.getPicName();
        picture.setName(picName);
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());

        // 如果是更新
        if (pictureId != null && pictureId > 0){
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }

        // 补充审核参数
        this.fillReviewParam(picture, loginUser);

        // 入库
        boolean result = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败，入库失败");
        return PictureVO.objToVo(picture);
    }

    /**
     * 校验图片
     *
     * @param picture
     */
    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        // 修改数据时，id 不能为空，有参数则校验
        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id 不能为空");
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url 过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }


    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (pictureQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();
        // TODO:审核时间范围查询
        Date reviewTime = pictureQueryRequest.getReviewTime();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();
        // 从多字段中搜索
        if (StrUtil.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("name", searchText)
                    .or()
                    .like("introduction", searchText)
            );
        }
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);
        // JSON 数组查询
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
        PictureVO pictureVO = PictureVO.objToVo(picture);
        Long userId = picture.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            if (user != null) {
                UserVO userVO = userService.getUserVO(user);
                pictureVO.setUser(userVO);
            }
        }
        return pictureVO;
    }

    /**
     * 分页获取图片封装
     */
    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
        // 对象列表 => 封装对象列表
        List<PictureVO> pictureVOList = pictureList.stream().map(PictureVO::objToVo).collect(Collectors.toList());
        // 1. 关联查询用户信息
        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVO.setUser(userService.getUserVO(user));
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

    @Override
    public void reviewPicture(PictureReviewRequest pictureReviewRequest, User loginUser) {
        // 1、校验参数
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR, "参数为空");
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        PictureReviewStatusEnum reviewStatusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        if (reviewStatusEnum == null || PictureReviewStatusEnum.REVIEWING.equals(reviewStatusEnum))
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误，状态不存在");

        // 2、判断图片是否存在
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        // 3、校验审核状态是否重复
        ThrowUtils.throwIf(oldPicture.getReviewStatus().equals(reviewStatusEnum), ErrorCode.PARAMS_ERROR, "请勿重复审核");

        // 4、修改审核状态（数据库操作）
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureReviewRequest, picture);
        picture.setReviewTime(new Date());
        boolean result = this.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "更新数据库失败，审核失败");

    }


    /**
     * @description:  填充审核参数
     * @param[1] picture
     * @param[2] loginUser
     * @throws:
     * @return:
     */
    @Override
    public void fillReviewParam(Picture picture, User loginUser){
        if (userService.isAdmin(loginUser)){
            // 管理员自动过审
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewMessage("管理员自动过审");
            picture.setReviewerId(loginUser.getId());
            picture.setReviewTime(new Date());
        } else {
            // 非管理员，编辑或创建 默认待审核
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }

    /**
     * @description:  批量抓取图片上传
     * @param[1] pictureUploadByBatchRequest
     * @param[2] loginUser
     * @throws:
     * @return:
     */
    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        // 校验参数
        ThrowUtils.throwIf(pictureUploadByBatchRequest == null, ErrorCode.PARAMS_ERROR, "参数为空");
        String searchText = pictureUploadByBatchRequest.getSearchText();
        Integer count = pictureUploadByBatchRequest.getCount();
        String namePrefix = pictureUploadByBatchRequest.getNamePrefix();
        ThrowUtils.throwIf(count > 30, ErrorCode.PARAMS_ERROR, "最多30条");

        // 名称前缀默认等于搜索词
        if (StringUtils.isBlank(namePrefix))
            namePrefix = searchText;
        // 抓取内容
        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", searchText);
        Document document;
        try {
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            log.error("获取页面失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取页面失败");
        }
        Element div = document.getElementsByClass("dgControl").first();
        if (ObjUtil.isNull(div)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取元素失败");
        }
        Elements imgElementList = div.select("img.mimg");

        // 记录上传成功图片数
        int uploadCount = 0;

        for (Element imgElement : imgElementList) {
            String fileUrl = imgElement.attr("src");
            if (StringUtils.isBlank(fileUrl)) {
                log.info("图片地址为空，已跳过: {}", fileUrl);
                continue;
            }

            // 处理图片上传地址，防止转义问题
            int questionMarkIndex = fileUrl.indexOf("?");
            if (questionMarkIndex != -1) {
                fileUrl = fileUrl.substring(0, questionMarkIndex);
            }
            // 上传图片
            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            pictureUploadRequest.setFileUrl(fileUrl);
            // 构造图片名称
            pictureUploadRequest.setPicName(namePrefix + (uploadCount + 1));
            try {

                PictureVO pictureVO = this.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
                log.info("图片上传成功: {}", pictureVO.getId());
                uploadCount++;
            } catch (Exception e) {
                log.error("图片上传失败", e);
                continue;
            }

            if (uploadCount >= count) {
                break;
            }
        }
        return uploadCount;
    }
}




