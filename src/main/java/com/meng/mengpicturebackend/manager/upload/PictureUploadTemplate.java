package com.meng.mengpicturebackend.manager.upload;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.meng.mengpicturebackend.config.CosClientConfig;
import com.meng.mengpicturebackend.exception.BusinessException;
import com.meng.mengpicturebackend.exception.ErrorCode;
import com.meng.mengpicturebackend.manager.CosManager;
import com.meng.mengpicturebackend.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * @DESCRIPTION: 通用上传文件模板
 * @AUTHOR: MENGLINGQI
 * @TIME: 2025/2/17 23:08
 **/
@Slf4j
public abstract class PictureUploadTemplate {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;


    /**
     * @description: 上传图片模板方法，定义上传流程
     * @param: inputSource 输入源
     * @param: uploadPathPrefix 上传文件前缀
     * @throws:
     * @return:
     */
    public final UploadPictureResult uploadPicture(Object inputSource, String uploadPathPrefix) {
        // 1、校验图片
        validPicture(inputSource);
        // 2、图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originalFilename = getOriginalFilename(inputSource);
        // 自己拼接文件上传路径，防止用户上传文件名存在特殊字符造成与浏览器不兼容问题
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFileName);
        // 解析结果并返回
        File file = null;
        try {
            // 3、创建临时文件
            file = File.createTempFile(uploadPath, null);
            processFile(file, inputSource);
            // 4、上传图片到对象存储（增加压缩处理规则）
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            // 5、封装返回结果
            List<CIObject> objectList = processResults.getObjectList();
            if (CollUtil.isNotEmpty(objectList)) {
                CIObject compressCiObject = objectList.get(0);

                // 缩略图默认等于压缩后图片
                CIObject thumnailCiObject = compressCiObject;
                if (objectList.size() > 1){
                    // 有缩略图才取
                    thumnailCiObject = objectList.get(1);
                }
                // 封装压缩图返回结果
                return getUploadPictureResult(originalFilename, compressCiObject, thumnailCiObject);
            }
            return getUploadPictureResult(originalFilename, uploadPath, file, imageInfo);
        } catch (Exception e) {
            log.error("file upload error, filpath = " + uploadPath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            // 6、删除临时文件
            deleteTempFile(file);
        }
    }

    /**
     * @description:  校验输入源（ 文件 或者 URL ）
     * @param[1] inputSource
     * @throws:
     * @return:
     */
    public abstract void validPicture(Object inputSource);

    /**
     * @description: 获取原始文件名
     * @param[1] inputSource
     * @throws:
     * @return:
     */
    public abstract String getOriginalFilename(Object inputSource);

    /**
     * @description: 处理输入源并生成临时文件
     * @param[1] file 临时文件
     * @param[2] inputSource
     * @throws:
     * @return:
     */
    public abstract void processFile(File file, Object inputSource) throws Exception;

    /**
     * @description: 封装返回结果
     * @param[1] originalFilename
     * @param[2] uploadPath
     * @param[3] file
     * @param[4] imageInfo
     * @throws:
     * @return:
     */
    private UploadPictureResult getUploadPictureResult(String originalFilename, String uploadPath, File file, ImageInfo imageInfo) {
        // 计算宽高
        int picWidth = imageInfo.getWidth();
        int picHeight = imageInfo.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();

        // 封装返回结果
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicSize(FileUtil.size(file));
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(imageInfo.getFormat());

        return uploadPictureResult;
    }

    /**
     * @description:
     * @param[1] originalFilename 图片名称
     * @param[2] compressCiObject 压缩后图片信息
     * @throws:
     * @return:
     */
    private UploadPictureResult getUploadPictureResult(String originalFilename, CIObject compressCiObject, CIObject thumnailCiObject) {
        // 计算宽高
        int picWidth = compressCiObject.getWidth();
        int picHeight = compressCiObject.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();

        // 封装返回结果
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        // 图片压缩后的信息
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + compressCiObject.getKey());
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicSize(compressCiObject.getSize().longValue());
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(compressCiObject.getFormat());

        // 设置缩略图
        uploadPictureResult.setThumbnailUrl(cosClientConfig.getHost() + "/" + thumnailCiObject.getKey());

        return uploadPictureResult;
    }

    /**
     * @description: 删除临时文件
     * @param[1] file
     * @throws:
     * @return:
     */
    private void deleteTempFile(File file) {
        if (file == null) {
            return;
        }
        boolean delete = file.delete();
        if (!delete) {
            log.error("file delete error, file = {}", file.getAbsolutePath());
        }
    }
}
