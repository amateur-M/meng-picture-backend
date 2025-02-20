package com.meng.mengpicturebackend.manager;

import com.meng.mengpicturebackend.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;

/**
 * @DESCRIPTION: COS通用能力类
 * @AUTHOR: MENGLINGQI
 * @TIME: 2025/2/17 23:08
 **/
@Component
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;


    /**
     * 上传对象
     *
     * @param key  唯一键
     * @param file 文件
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 下载文件
     *
     * @param key  唯一键
     */
    public COSObject getObject(String key){
        return cosClient.getObject(cosClientConfig.getBucket(), key);
    }

    /**
     * 上传对象并进行解析
     *
     * @param key  唯一键
     * @param file 文件
     */
    public PutObjectResult putPictureObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);

        // 对图片进行处理（获取图片基本信息）
        // 定义图片处理操作对象
        PicOperations picOperations = new PicOperations();
        // 1 表示返回原图片所有信息
        picOperations.setIsPicInfo(1);
        // 设置图片处理规则
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }

}
