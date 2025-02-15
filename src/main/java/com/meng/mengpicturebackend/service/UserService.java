package com.meng.mengpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.meng.mengpicturebackend.model.dto.user.UserQueryRequest;
import com.meng.mengpicturebackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.meng.mengpicturebackend.model.vo.LoginUserVO;
import com.meng.mengpicturebackend.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取加密密码
     *
     * @param userPassword
     * @return
     */
    String getEncryptPassword(String userPassword);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取脱敏的已登录用户信息
     *
     * @param user
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLoginOut(HttpServletRequest request);

    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏的用户信息列表
     *
     * @param userList
     * @return
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 根据请求组装成查询对象类
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

}
