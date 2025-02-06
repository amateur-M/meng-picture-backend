package com.meng.mengpicturebackend.common;

import lombok.Data;

/**
 * @DESCRIPTION: 分页请求包装类
 * @AUTHOR: MENGLINGQI
 * @TIME: 2025/2/6 23:43
 **/
@Data
public class PageRequest {

    /**
     * 当前页号
     */
    private int current = 1;

    /**
     * 页面大小
     */
    private int pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（默认降序）
     */
    private String sortOrder = "descend";
}

