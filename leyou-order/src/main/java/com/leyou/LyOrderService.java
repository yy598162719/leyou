package com.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author: HuYi.Zhang
 * @create: 2018-05-04 09:36
 **/
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class LyOrderService {

    public static void main(String[] args) {
        SpringApplication.run(LyOrderService.class, args);
    }
}
