package com.atguigu.yygh.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.atguigu.yygh")
@MapperScan("com.atguigu.yygh.user.mapper")
@EnableDiscoveryClient
@EnableFeignClients("com.atguigu")
@EnableConfigurationProperties(value = com.atguigu.yygh.user.prop.WeixinProperties.class)
public class UserMainStrater {
    public static void main(String[] args) {
        SpringApplication.run(UserMainStrater.class,args);
    }
}
