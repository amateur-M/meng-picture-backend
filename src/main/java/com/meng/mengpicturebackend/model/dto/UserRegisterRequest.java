package com.meng.mengpicturebackend.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @DESCRIPTION: 用户注册请求类
 * @AUTHOR: MENGLINGQI
 * @TIME: 2025/2/11 10:06
 **/
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 确认密码
     */
    private String checkPassword;
}
