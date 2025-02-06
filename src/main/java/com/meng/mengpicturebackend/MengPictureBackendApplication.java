package com.meng.mengpicturebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.meng.mengpicturebackend.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class MengPictureBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MengPictureBackendApplication.class, args);
    }

}
