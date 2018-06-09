package com.leyou.goodsDetail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author Qin PengCheng
 * @date 2018/6/9
 */
@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication
public class goodsDetail {

    public static void main(String[] args){
        SpringApplication.run(goodsDetail.class);
    }
}
