package com.meng.mengpicturebackend.common;

import com.meng.mengpicturebackend.exception.ErrorCode;
import lombok.Data;

/**
 * @DESCRIPTION: 全局响应对象
 * @AUTHOR: MENGLINGQI
 * @TIME: 2025/2/6 23:17
 **/
@Data
public class BaseResponse<T> {
    private int code;

    private T data;

    private String message;

    /**
     * @description:  定义多种构造函数，应对后续不同结果返回形式
     * @throws:
     * @return:
     */

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(int code, T data) {
        // 同一个类中，调用同一个类的构造函数用 this
        this(code, data, "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}
