package com.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

/**
 * @author Qin PengCheng
 * @date 2018/5/24
 */
@SpringBootApplication
@EnableEurekaClient
@EnableZuulProxy
public class ApiGetWay {

    public static void main(String[] args){
        SpringApplication.run(ApiGetWay.class,args);
    }
}
