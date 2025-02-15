package com.meng.mengpicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @DESCRIPTION: 用户增加请求类
 * @AUTHOR: MENGLINGQI
 * @TIME: 2025/2/15 22:55
 **/
@Data
public class UserAddRequest implements Serializable {

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色: user, admin
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}

