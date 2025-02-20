package com.meng.mengpicturebackend.service;

import com.meng.mengpicturebackend.model.dto.picture.PictureUploadRequest;
import com.meng.mengpicturebackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.meng.mengpicturebackend.model.entity.User;
import com.meng.mengpicturebackend.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

/**
* @author menglingqi
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-02-18 14:17:38
*/
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片
     *
     * @param multipartFile
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(MultipartFile multipartFile,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);


}
