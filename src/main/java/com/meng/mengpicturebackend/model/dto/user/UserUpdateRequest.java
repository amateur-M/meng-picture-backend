package com.meng.mengpicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @DESCRIPTION: 用户更新请求类
 * @AUTHOR: MENGLINGQI
 * @TIME: 2025/2/15 22:55
 **/
@Data
public class UserUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}


