package com.leyou.user.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @author Qin PengCheng
 * @date 2018/6/11
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.leyou.user.service.mapper")
public class LeYouUserService {

    public static void main(String[] args){
        SpringApplication.run(LeYouUserService.class);
    }

}
