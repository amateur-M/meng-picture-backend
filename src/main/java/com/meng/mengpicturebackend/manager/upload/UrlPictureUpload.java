package com.meng.mengpicturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.meng.mengpicturebackend.exception.BusinessException;
import com.meng.mengpicturebackend.exception.ErrorCode;
import com.meng.mengpicturebackend.exception.ThrowUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * @DESCRIPTION: 通过图片URL上传图片
 * @AUTHOR: MENGLINGQI
 * @TIME: 2025/3/16 22:07
 **/
@Service
public class UrlPictureUpload extends PictureUploadTemplate {
    @Override
    public void validPicture(Object inputSource) {
        String fileUrl = (String) inputSource;
        // 校验非空
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "文件地址不能为空");
        // 校验 URL 格式
        try {
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件地址格式错误");
        }
        // 校验 URL 协议
        ThrowUtils.throwIf(!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://"), ErrorCode.PARAMS_ERROR, "文件地址协议错误，仅支持http或者https协议文件地址");
        // 发送HEAD请求验证文件是否存在
        try (HttpResponse response = HttpUtil.createRequest(Method.HEAD, fileUrl).execute()){
            if (response.getStatus() != HttpStatus.HTTP_OK) {
                // 控制校验的合理性，例如服务器不支持HEAD请求，则不进行后续校验
                return;
            }
            // 如果文件存在，校验文件类型、文件大小
            String contentType = response.header("Content-Type");
            if (StrUtil.isNotBlank(contentType)){
                final List<String> ALLOW_CONTENT_TYPE = Arrays.asList("image/png", "image/jpg", "image/jpeg", "image/webp");
                ThrowUtils.throwIf(!ALLOW_CONTENT_TYPE.contains(contentType), ErrorCode.PARAMS_ERROR, "不支持的文件类型");
            }

            String contentLengthStr = response.header("Content-Length");
            if (StrUtil.isNotBlank(contentLengthStr)){
                try{
                    final long maxFileSize = 1024 * 1024 * 2;
                    long contentLength = Long.parseLong(contentLengthStr);
                    ThrowUtils.throwIf(contentLength > maxFileSize, ErrorCode.PARAMS_ERROR, "文件大小不能超过2M");
                } catch (NumberFormatException e){
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小格式错误");
                }
            }
        }
    }

    @Override
    public String getOriginalFilename(Object inputSource) {
        String fileUrl = (String) inputSource;
        return FileUtil.mainName(fileUrl);
    }

    @Override
    public void processFile(File file, Object inputSource) throws Exception {
        String fileUrl = (String) inputSource;
        // 通过URL获取（下载）到图片
        HttpUtil.downloadFileFromUrl(fileUrl, file);
    }
}
