package com.atguigu.yygh.user.prop;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "weixin")
@Data
public class WeixinProperties {

    //将yaml文件属性绑定到实体类的方式：
    //1.使用@Value注解和@Component
    //2.在实体类上面加上@Component 和 @ConfigurationProperties(prefix="")注解 设置prefix属性 注意：实体类属性要与properties的属性一致
    //3.在启动类上加上@EnableConfigurationProperties注解设置value属性为实体类的字节码 和在实体类上面加上@ConfigurationProperties(prefix="")注解

    private String appid;

    private String appsecret;

    private String redirecturl;

}
