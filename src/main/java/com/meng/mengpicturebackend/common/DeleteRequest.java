package com.meng.mengpicturebackend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @DESCRIPTION: 删除请求包装类
 * @AUTHOR: MENGLINGQI
 * @TIME: 2025/2/6 23:43
 **/
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}

