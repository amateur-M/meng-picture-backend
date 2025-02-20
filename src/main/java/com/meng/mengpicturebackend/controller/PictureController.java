package com.meng.mengpicturebackend.controller;

import com.meng.mengpicturebackend.annotation.AuthCheck;
import com.meng.mengpicturebackend.common.BaseResponse;
import com.meng.mengpicturebackend.common.ResultUtils;
import com.meng.mengpicturebackend.constant.UserConstant;
import com.meng.mengpicturebackend.model.dto.picture.PictureUploadRequest;
import com.meng.mengpicturebackend.model.entity.User;
import com.meng.mengpicturebackend.model.vo.PictureVO;
import com.meng.mengpicturebackend.service.PictureService;
import com.meng.mengpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/upload")
    public BaseResponse<PictureVO> uploadPicture(@RequestPart("file") MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }


}
