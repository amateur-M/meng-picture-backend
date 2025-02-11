package com.meng.mengpicturebackend.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meng.mengpicturebackend.exception.BusinessException;
import com.meng.mengpicturebackend.exception.ErrorCode;
import com.meng.mengpicturebackend.model.entity.User;
import com.meng.mengpicturebackend.model.enums.UserRoleEnum;
import com.meng.mengpicturebackend.service.UserService;
import com.meng.mengpicturebackend.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
* @author MENGLINGQI
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-02-10 22:55:30
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    /**
     * 用户注册
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1、校验参数
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        // 2、检查用户账号是否已存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已存在");
        }
        // 3、密码加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 4、插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("用户" + userAccount);
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = this.save(user);
        if (!saveResult){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }

        // 返回用户id mybatis-plus 主键回填特性
        return user.getId();
    }


    /**
     * @description:  加密用户密码
     * * @param[1] userPassword
     * @throws:
     * @return:
     */
    @Override
    public String getEncryptPassword(String userPassword) {
        // 盐值
        final String SALT = "meng";
        // 密码加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        return encryptPassword;
    }
}




