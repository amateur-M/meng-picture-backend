package com.meng.mengpicturebackend.model.vo;

import lombok.Data;

import java.util.List;

/**
 * @DESCRIPTION: 标签列表对象
 * @AUTHOR: MENGLINGQI
 * @TIME: 2025/3/2 11:49
 **/
@Data
public class PictureTagCategory {
    /**
     * 标签列表
     */
    List<String> tagList;

    /**
     * 分类列表
     */
    List<String> categoryList;
}
