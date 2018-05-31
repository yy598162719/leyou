package com.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @author Qin PengCheng
 * @date 2018/5/24
 */
@SpringBootApplication
@EnableEurekaClient
@EnableTransactionManagement
@MapperScan("com.leyou.item.mapper")
public class ItemService {

    public static void main(String[] args){
        SpringApplication.run(ItemService.class,args);
    }
}
