package com.atguigu.yygh.cmn;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan(value = "com.atguigu.yygh.cmn.mapper")
@ComponentScan(value = "com.atguigu.yygh")
@EnableDiscoveryClient
public class CmnMainStarter {
    public static void main(String[] args) {
        SpringApplication.run(CmnMainStarter.class,args);
    }
}
