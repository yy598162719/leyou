package com.leyou.upload;

import javafx.application.Application;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @author Qin PengCheng
 * @date 2018/5/30
 */
@EnableEurekaClient
@SpringBootApplication
public class leyouUploadApplication {
    public static void main(String[] args){
        SpringApplication.run(leyouUploadApplication.class);
    }
}
