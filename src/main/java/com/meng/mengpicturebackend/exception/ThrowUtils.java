package com.meng.mengpicturebackend.exception;

/**
 * @DESCRIPTION: 异常处理工具类
 * @AUTHOR: MENGLINGQI
 * @TIME: 2025/2/6 16:49
 **/
public class ThrowUtils {

    /**
     * @description:  条件成立抛异常
     * @param[1] condition
     * @param[2] runtimeException
     * @throws:
     * @return:
     */
    public static void throwIf(boolean condition, RuntimeException runtimeException) {
        if (condition)
            throw runtimeException;
    }

    /**
     * @description:  条件成立抛异常
     * @param[1] condition
     * @param[2] errorCode
     * @throws:
     * @return:
     */
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        throwIf(condition, new BusinessException(errorCode));
    }

    /**
     * @description:  条件成立抛异常
     * @param[1] condition
     * @param[2] errorCode
     * @param[3] message
     * @throws:
     * @return:
     */
    public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        throwIf(condition, new BusinessException(errorCode, message));
    }


}
