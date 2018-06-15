package com.leyou.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author Qin PengCheng
 * @date 2018/6/13
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class LeYouAuthService {

    public static void main(String[] args){
        SpringApplication.run(LeYouAuthService.class);
    }

}
