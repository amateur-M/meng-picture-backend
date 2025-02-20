package com.meng.mengpicturebackend.aop;

import com.meng.mengpicturebackend.annotation.AuthCheck;
import com.meng.mengpicturebackend.exception.BusinessException;
import com.meng.mengpicturebackend.exception.ErrorCode;
import com.meng.mengpicturebackend.model.entity.User;
import com.meng.mengpicturebackend.model.enums.UserRoleEnum;
import com.meng.mengpicturebackend.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @DESCRIPTION: 注解拦截器
 * @AUTHOR: MENGLINGQI
 * @TIME: 2025/2/15 21:57
 **/
@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    /**
     * @description:  执行拦截
     * @param[1] proceedingJoinPoint
     * @param[2] authCheck
     * @throws:
     * @return:
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint proceedingJoinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();

        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 更优雅获取请求写法：(HttpServletRequest) requestAttributes.resolveReference(RequestAttributes.REFERENCE_REQUEST);

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        // 使用AuthCheck注解时定义的角色
        UserRoleEnum mustUserRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        if (mustUserRoleEnum == null) {
            // 定义的角色不存在，继续执行
            return proceedingJoinPoint.proceed();
        }

        // 用户必须有对应权限才会通过
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        // 如果用户没有权限 或者 用户权限小于需要权限 抛异常
        if (userRoleEnum == null || mustUserRoleEnum.getLevel() > userRoleEnum.getLevel())
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);

        return proceedingJoinPoint.proceed();
    }


}
