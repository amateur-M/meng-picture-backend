package com.meng.mengpicturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import com.meng.mengpicturebackend.exception.ErrorCode;
import com.meng.mengpicturebackend.exception.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * @DESCRIPTION: 通过本地文件上传图片实现
 * @AUTHOR: MENGLINGQI
 * @TIME: 2025/3/16 22:04
 **/
@Service
public class FilePictureUpload extends PictureUploadTemplate {
    @Override
    public void validPicture(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        // 1、校验文件大小
        long fileSize = multipartFile.getSize();
        final long maxFileSize = 1024 * 1024 * 2;
        ThrowUtils.throwIf(fileSize > maxFileSize, ErrorCode.PARAMS_ERROR, "文件大小不能超过2M");
        // 2、校验文件类型
        String filesuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        // 允许上传的文件类型
        final List<String> fileSuffixList = Arrays.asList("png", "jpg", "jpeg", "webp");
        ThrowUtils.throwIf(!fileSuffixList.contains(filesuffix), ErrorCode.PARAMS_ERROR, "不支持的文件类型");
    }

    @Override
    public String getOriginalFilename(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        return multipartFile.getOriginalFilename();
    }

    @Override
    public void processFile(File file, Object inputSource) throws Exception {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        multipartFile.transferTo(file);
    }
}
