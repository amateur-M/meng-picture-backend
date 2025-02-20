package com.meng.mengpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * @DESCRIPTION: 图片上传请求包装类
 * @AUTHOR: MENGLINGQI
 * @TIME: 2025/2/18 14:28
 **/
@Data
public class PictureUploadRequest implements Serializable {

    /**
     * 图片id（用于修改、重复上传）
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}
