package com.meng.mengpicturebackend.service;

import com.meng.mengpicturebackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author MENGLINGQI
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-02-10 22:55:30
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 获取加密密码
     *
     * @param userPassword
     * @return
     */
    String getEncryptPassword(String userPassword);
}
