package com.leyou.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author Qin PengCheng
 * @date 2018/6/5
 */
/*exclude = {DataSourceAutoConfiguration.class}*/
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class LeYouSearchApplication {

    public static void main(String[] args){
        SpringApplication.run(LeYouSearchApplication.class);
    }
}
