package com.heima;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author: HuangMuChen
 * @date: 2021/2/3 15:35
 * @version: V1.0
 * @Description: 后台管理微服务引导类
 */
@SpringBootApplication
@ServletComponentScan("com.heima.common.web.admin.security")
@MapperScan("com.heima.admin.dao") // mapper接口的包扫描
@EnableScheduling // 开启定时任务
public class AdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }
}
