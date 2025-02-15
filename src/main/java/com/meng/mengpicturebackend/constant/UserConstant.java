package com.meng.mengpicturebackend.constant;

public interface UserConstant {

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "user_login";

    /**
     * 用户注册盐值
     */
    String USER_REGISTER_SALT = "meng";

    /**
     * 用户注册默认密码
     */
    String USER_DEFAULT_PASSWORD = "12345678";

    //  region 权限

    /**
     * 默认角色
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     */
    String ADMIN_ROLE = "admin";

    // endregion
}
