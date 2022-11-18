package com.atguigu.yygh.oss.cofig;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@PropertySource(value = "classpath:OssProperties.properties",encoding = "utf-8")
@ConfigurationProperties(prefix = "oss")
@Component
@Data
public class OssProperties {

    private String endpoint;

    private String AccessKeyId;

    private String AccessKeySecret;

    private String bucketName;
}
