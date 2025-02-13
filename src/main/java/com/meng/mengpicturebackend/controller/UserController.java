package com.meng.mengpicturebackend.controller;

import com.meng.mengpicturebackend.common.BaseResponse;
import com.meng.mengpicturebackend.common.ResultUtils;
import com.meng.mengpicturebackend.exception.ErrorCode;
import com.meng.mengpicturebackend.exception.ThrowUtils;
import com.meng.mengpicturebackend.model.dto.UserLoginRequest;
import com.meng.mengpicturebackend.model.dto.UserRegisterRequest;
import com.meng.mengpicturebackend.model.vo.LoginUserVO;
import com.meng.mengpicturebackend.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @DESCRIPTION: 用户controller
 * @AUTHOR: MENGLINGQI
 * @TIME: 2025/2/7 16:06
 **/
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();

        long userRegister = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(userRegister);

    }

    /**
     * 用户登录
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userloginRequest, HttpServletRequest request){
        ThrowUtils.throwIf(userloginRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userloginRequest.getUserAccount();
        String userPassword = userloginRequest.getUserPassword();
        LoginUserVO userRegister = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(userRegister);
    }


}
