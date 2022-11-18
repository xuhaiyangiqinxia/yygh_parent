package com.atguigu.yygh.hosp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan(value = "com.atguigu.yygh.hosp.mapper")
@ComponentScan(value = "com.atguigu.yygh")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.atguigu.yygh")
public class HospStartMain {
    public static void main(String[] args) {
        SpringApplication.run(HospStartMain.class,args);
    }
}
