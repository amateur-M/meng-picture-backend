package com.meng.mengpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.meng.mengpicturebackend.model.dto.picture.PictureQueryRequest;
import com.meng.mengpicturebackend.model.dto.picture.PictureReviewRequest;
import com.meng.mengpicturebackend.model.dto.picture.PictureUploadRequest;
import com.meng.mengpicturebackend.model.entity.Picture;
import com.meng.mengpicturebackend.model.entity.User;
import com.meng.mengpicturebackend.model.vo.PictureVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author menglingqi
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-02-18 14:17:38
*/
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片
     *
     * @param inputSource
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(Object inputSource,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);

    /**
     * 校验图片
     *
     * @param picture
     */
    void validPicture(Picture picture);

    /**
     * 根据请求组装成查询对象类
     *
     * @param pictureQueryRequest
     * @return
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取图片脱敏对象（单条）
     *
     * @param picture
     * @param request
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 获取图片脱敏对象（分页）
     *
     * @param picturePage
     * @param request
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 图片审核
     * @param pictureReviewRequest
     * @param loginUser
     */
    void reviewPicture(PictureReviewRequest pictureReviewRequest, User loginUser);


    void fillReviewParam(Picture picture, User loginUser);
}
