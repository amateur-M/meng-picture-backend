package com.meng.mengpicturebackend.controller;

import com.meng.mengpicturebackend.common.BaseResponse;
import com.meng.mengpicturebackend.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @DESCRIPTION: 初始controller
 * @AUTHOR: MENGLINGQI
 * @TIME: 2025/2/7 16:06
 **/
@RestController
public class MainController {

    @GetMapping("/health")
    public BaseResponse<String> health(){
        Map aa = new HashMap<>();
        ArrayList list = new ArrayList();
        return ResultUtils.success("ok");
    }


}
