package com.meng.mengpicturebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meng.mengpicturebackend.exception.ErrorCode;
import com.meng.mengpicturebackend.exception.ThrowUtils;
import com.meng.mengpicturebackend.manager.FileManager;
import com.meng.mengpicturebackend.model.dto.file.UploadPictureResult;
import com.meng.mengpicturebackend.model.dto.picture.PictureUploadRequest;
import com.meng.mengpicturebackend.model.entity.Picture;
import com.meng.mengpicturebackend.model.entity.User;
import com.meng.mengpicturebackend.model.vo.PictureVO;
import com.meng.mengpicturebackend.service.PictureService;
import com.meng.mengpicturebackend.mapper.PictureMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Date;

/**
* @author menglingqi
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2025-02-18 14:17:38
*/
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService{

    @Resource
    FileManager fileManager;

    /**
     * @description:  上传图片 --> 数据万象解析 --> 填充其他信息 --> 数据库入库
     * @param[1] multipartFile
     * @param[2] pictureUploadRequest
     * @param[3] loginUser
     * @throws:
     * @return:
     */
    @Override
    public PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser) {
        // 用于判断是更新还是上传
        Long pictureId = null;
        if (pictureUploadRequest != null)
            pictureId = pictureUploadRequest.getId();

        // 如果更新图片，校验参数是否存在
        if (pictureId != null && pictureId > 0) {
            // 查询数据库
            boolean exists = this.lambdaQuery().eq(Picture::getId, pictureId).exists();
            ThrowUtils.throwIf(!exists, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        }
        // 上传图片得到信息
        // 根据用户划分目录
        String filePrefix = String.format("public/%s", loginUser.getId());
        UploadPictureResult uploadPictureResult = fileManager.uploadPicture(multipartFile, filePrefix);
        // 构造入库信息
        Picture picture = new Picture();

        picture.setUrl(uploadPictureResult.getUrl());
        picture.setName(uploadPictureResult.getPicName());
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());

        // 如果是更新
        if (pictureId != null && pictureId > 0){
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }

        // 入库
        boolean result = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败，入库失败");
        return PictureVO.objToVo(picture);
    }
}




