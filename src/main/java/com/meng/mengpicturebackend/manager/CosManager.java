package com.meng.mengpicturebackend.manager;

import cn.hutool.core.io.FileUtil;
import com.meng.mengpicturebackend.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
        // 上传请求对象
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);

        // 构造图片处理规则
        List<PicOperations.Rule> ruleList = new ArrayList<>();

        // 1、图片压缩 转为 webp 格式
        String webpKey = FileUtil.mainName(key) + ".webp";
        PicOperations.Rule compressRule = new PicOperations.Rule();
        compressRule.setRule("imageMogr2/format/webp");
        compressRule.setBucket(cosClientConfig.getBucket());
        compressRule.setFileId(webpKey);
        ruleList.add(compressRule);

        // 因较小图片生成缩略图后体积反而变大，故增加校验
        if (file.length() > 1024 * 1024){
            // 2、缩略图处理规则
            String thumbnailKey = FileUtil.mainName(key) + "_thumbnail." + FileUtil.getSuffix(key);
            PicOperations.Rule thumbnailRule = new PicOperations.Rule();
            thumbnailRule.setBucket(cosClientConfig.getBucket());
            thumbnailRule.setFileId(thumbnailKey);
            thumbnailRule.setRule(String.format("imageMogr2/thumbnail/!%sx%s>", 128, 128));
            ruleList.add(thumbnailRule);
        }

        // 定义图片处理操作对象
        PicOperations picOperations = new PicOperations();
        // 1 表示返回原图片所有信息
        picOperations.setIsPicInfo(1);
        picOperations.setRules(ruleList);
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }

}
