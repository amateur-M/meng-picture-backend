package com.meng.mengpicturebackend.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.meng.mengpicturebackend.annotation.AuthCheck;
import com.meng.mengpicturebackend.common.BaseResponse;
import com.meng.mengpicturebackend.common.DeleteRequest;
import com.meng.mengpicturebackend.common.ResultUtils;
import com.meng.mengpicturebackend.constant.UserConstant;
import com.meng.mengpicturebackend.exception.BusinessException;
import com.meng.mengpicturebackend.exception.ErrorCode;
import com.meng.mengpicturebackend.exception.ThrowUtils;
import com.meng.mengpicturebackend.model.dto.picture.*;
import com.meng.mengpicturebackend.model.entity.Picture;
import com.meng.mengpicturebackend.model.entity.User;
import com.meng.mengpicturebackend.model.enums.PictureReviewStatusEnum;
import com.meng.mengpicturebackend.model.vo.PictureTagCategory;
import com.meng.mengpicturebackend.model.vo.PictureVO;
import com.meng.mengpicturebackend.service.PictureService;
import com.meng.mengpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @DESCRIPTION: 图片相关操作接口类
 * @AUTHOR: MENGLINGQI
 * @TIME: 2025/2/20 9:33
 **/
@Slf4j
@RestController
@RequestMapping("/picture")
public class PictureController {

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    /**
     * caffeine 本地缓存
     */
    private final Cache<String, String> LOCAL_CACHE =
            Caffeine.newBuilder().initialCapacity(1024)
                    .maximumSize(10000L)
                    // 缓存 5 分钟移除
                    .expireAfterWrite(5L, TimeUnit.MINUTES)
                    .build();


    /**
     * @description:  上传图片
     * @param[1] multipartFile
     * @param[2] pictureUploadRequest
     * @param[3] request
     * @throws:
     * @return:
     */
    @PostMapping("/upload")
    public BaseResponse<PictureVO> uploadPicture(@RequestPart("file") MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * @description:  通过 URL 上传图片
     * @param[1] pictureUploadRequest
     * @param[2] request
     * @throws:
     * @return:
     */
    @PostMapping("/upload/url")
    public BaseResponse<PictureVO> uploadPictureByUrl(@RequestBody PictureUploadRequest pictureUploadRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String fileUrl = pictureUploadRequest.getFileUrl();
        PictureVO pictureVO = pictureService.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * @description:  删除图片
     * @param[1] request
     * @param[2] deleteRequest
     * @throws:
     * @return:
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(HttpServletRequest request, @RequestBody DeleteRequest deleteRequest) {
        // 参数校验
        if (deleteRequest.getId() <= 0 || deleteRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 判断图片是否存在，且只有本人或者管理员能删除
        User loginUser = userService.getLoginUser(request);
        Long deletePictureId = deleteRequest.getId();
        Picture picture = pictureService.getById(deletePictureId);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        if (!picture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser))
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有权限删除");

        // 操作数据库
        boolean isRemoved = pictureService.removeById(deletePictureId);
        ThrowUtils.throwIf(!isRemoved, ErrorCode.OPERATION_ERROR, "删除失败");
        return ResultUtils.success(true);
    }

    /**
     * @description: 更新图片（仅管理员可用）
     * @param[1] pictureUpdateRequest
     * @throws:
     * @return:
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request) {
        // 参数校验
        if (pictureUpdateRequest.getId() <= 0 || pictureUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 将请求类转为实体类
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureUpdateRequest, picture);
        // 将 pictureUpdateRequest.getTags() 转换为 JSON 字符串
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        // 校验图片
        pictureService.validPicture(picture);
        // 判断图片是否存在
        Long oldPictureId = pictureUpdateRequest.getId();
        Picture oldPicture = pictureService.getById(oldPictureId);
        // 补充审核参数
        User loginUser = userService.getLoginUser(request);
        pictureService.fillReviewParam(picture, loginUser);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "要更新的图片不存在");
        // 操作数据库
        boolean isUpdated = pictureService.updateById(picture);
        ThrowUtils.throwIf(!isUpdated, ErrorCode.OPERATION_ERROR, "更新失败");
        return ResultUtils.success(true);
    }

    /**
     * @description:  根据id查询图片（管理员）
     * @param[1] id
     * @throws:
     * @return:
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(@RequestParam(required = false) Long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        return ResultUtils.success(picture);
    }

    /**
     * @description:  根据id查询图片（封装类）
     * @param[1] id
     * @throws:
     * @return:
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(@RequestParam(required = false) Long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        return ResultUtils.success(pictureService.getPictureVO(picture, null));
    }

    /**
     * @description:  分页获取图片（管理员可用）
     * @param[1] pictureQueryRequest
     * @throws:
     * @return:
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {

        long current = pictureQueryRequest.getCurrent();
        long pageSize = pictureQueryRequest.getPageSize();
        // 分页查询
        Page<Picture> picturePage = pictureService.page(new Page<>(current, pageSize), pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(picturePage);
    }

    /**
     * @description:  分页获取图片封装（普通用户）
     * @param[1] pictureQueryRequest
     * @throws:
     * @return:
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {

        long current = pictureQueryRequest.getCurrent();
        long pageSize = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页条数不能超过 20");
        // 设置审核状态为通过状态，适配场景：普通用户默认只能看见审核通过内容
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());

        // 分页查询
        Page<Picture> picturePage = pictureService.page(new Page<>(current, pageSize), pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request));
    }

    /**
     * @description:  分页获取图片封装（普通用户）有缓存
     * @param[1] pictureQueryRequest
     * @throws:
     * @return:
     */
    @PostMapping("/list/page/vo/cache")
    public BaseResponse<Page<PictureVO>> listPictureVOByPageWithCache(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {

        long current = pictureQueryRequest.getCurrent();
        long pageSize = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页条数不能超过 20");

        // 设置审核状态为通过状态，适配场景：普通用户默认只能看见审核通过内容
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());

        // 构造 caffeine + redis 多级缓存
        // 1.构造缓存key
        String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
        String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
        String cacheKey = String.format("mengPicture:getPictureVOByPage:%s", hashKey);

        // 2. 先从caffeine中取
        String cachedValue = LOCAL_CACHE.getIfPresent(cacheKey);
        if (cachedValue != null) {
            log.info("caffeine本地缓存命中：{}", cacheKey);
            Page<PictureVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
            return ResultUtils.success(cachedPage);
        } else {
            // 3. caffeine未命中从redis中取
            ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
            cachedValue = opsForValue.get(cacheKey);
            if (cachedValue != null) {
                // caffeine未命中，redis缓存命中
                log.info("caffeine未命中，redis缓存命中:{}", cacheKey);
                // 更新本地缓存
                LOCAL_CACHE.put(cacheKey, cachedValue);
                Page<PictureVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
                return ResultUtils.success(cachedPage);
            } else {
                // 4.caffeine + redis缓存均未命中
                log.info("caffeine + redis缓存均未命中:{}", cacheKey);
                // 分页查询数据库
                Page<Picture> picturePage = pictureService.page(new Page<>(current, pageSize), pictureService.getQueryWrapper(pictureQueryRequest));
                Page<PictureVO> pictureVOPage = pictureService.getPictureVOPage(picturePage, request);

                // 写入redis
                String cacheValue = JSONUtil.toJsonStr(pictureVOPage);
                // 缓存value是否需要转换
                // String cacheHexValue = DigestUtils.md5DigestAsHex(cacheValue.getBytes());

                // 构造缓存过期时间 5 - 10 分钟 （防止缓存雪崩）
                int expireTime = 5 * 60 + RandomUtil.randomInt(0, 300);
                opsForValue.set(cacheKey, cacheValue, expireTime, TimeUnit.SECONDS);

                // 写入本地缓存
                LOCAL_CACHE.put(cacheKey, cacheValue);

                return ResultUtils.success(pictureVOPage);
            }
        }
    }

    /**
     * @description:  编辑图片
     * @param[1] request
     * @param[2] pictureEditRequest
     * @throws:
     * @return:
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(HttpServletRequest request, @RequestBody PictureEditRequest pictureEditRequest) {
        if (pictureEditRequest == null || pictureEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditRequest, picture);
        // 类型不同，需要转换
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        // 设置编辑时间
        picture.setEditTime(new Date());
        User loginUser = userService.getLoginUser(request);
        // 补充审核参数
        pictureService.fillReviewParam(picture, loginUser);
        // 校验图片
        pictureService.validPicture(picture);
        // 判断是否存在
        Picture oldPicture = pictureService.getById(pictureEditRequest.getId());
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        // 仅本人或管理员可以编辑
        if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser))
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有权限编辑");

        // 操作数据库
        boolean isUpdated = pictureService.updateById(picture);
        ThrowUtils.throwIf(!isUpdated, ErrorCode.OPERATION_ERROR, "编辑失败");
        return ResultUtils.success(true);
    }

    /**
     * @description: 获取热门分类
     * @throws:
     * @return:
     */
    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }


    /**
     * @description:  审核图片
     * @param[1] request
     * @param[2] pictureReviewRequest
     * @throws:
     * @return:
     */
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> reviewPicture(HttpServletRequest request, @RequestBody PictureReviewRequest pictureReviewRequest) {
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.reviewPicture(pictureReviewRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * @description: 批量上传图片
     * @param[1] pictureUploadByBatchRequest
     * @param[2] request
     * @throws:
     * @return:
     */
    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureByBatch(
            @RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest,
            HttpServletRequest request
    ) {
        ThrowUtils.throwIf(pictureUploadByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        int uploadCount = pictureService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);
        return ResultUtils.success(uploadCount);
    }


}
