package com.meng.mengpicturebackend.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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

    @PostMapping("/upload")
    public BaseResponse<PictureVO> uploadPicture(@RequestPart("file") MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
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
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {

        long current = pictureQueryRequest.getCurrent();
        long pageSize = pictureQueryRequest.getPageSize();
        // 设置审核状态为通过状态，适配场景：普通用户默认只能看见审核通过内容
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());

        // 分页查询
        Page<Picture> picturePage = pictureService.page(new Page<>(current, pageSize), pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(pictureService.getPictureVOPage(picturePage, null));
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

}
