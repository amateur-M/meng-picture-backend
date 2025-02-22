package com.meng.mengpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @DESCRIPTION: 图片编辑请求包装类
 * @AUTHOR: MENGLINGQI
 * @TIME: 2025/2/18 14:28
 **/
@Data
public class PictureEditRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签
     */
    private List<String> tags;

    private static final long serialVersionUID = 1L;
}
