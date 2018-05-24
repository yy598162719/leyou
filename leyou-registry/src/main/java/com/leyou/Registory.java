package com.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @author Qin PengCheng
 * @date 2018/5/24
 */
@SpringBootApplication
@EnableEurekaServer
public class Registory {

    public static void main(String[] args){
        SpringApplication.run(Registory.class,args);
    }
}
