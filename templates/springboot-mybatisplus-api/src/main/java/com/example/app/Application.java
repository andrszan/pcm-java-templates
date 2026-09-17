package com.example.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * mapper 包在派生项目中按需创建，接口加 @Mapper 即可被扫描到；
 * 包不存在时仅记录警告，不影响启动。
 */
@SpringBootApplication
@MapperScan("com.example.app.mapper")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
