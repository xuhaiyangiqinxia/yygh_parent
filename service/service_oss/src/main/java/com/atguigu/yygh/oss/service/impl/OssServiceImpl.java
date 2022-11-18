package com.atguigu.yygh.oss.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.atguigu.yygh.oss.cofig.OssProperties;
import com.atguigu.yygh.oss.service.OssService;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@Slf4j
public class OssServiceImpl implements OssService {

    @Autowired
    private OssProperties ossProperties;

    @Override
    public String upload(MultipartFile file) {
        // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
        String endpoint = ossProperties.getEndpoint();
        log.info(endpoint);
        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        String accessKeyId = ossProperties.getAccessKeyId();
        log.info(accessKeyId);
        String accessKeySecret = ossProperties.getAccessKeySecret();
        log.info(accessKeySecret);
        // 填写Bucket名称，例如examplebucket。
        String bucketName = ossProperties.getBucketName();
        log.info(bucketName);


        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        String filename = UUID.randomUUID().toString().replaceAll("-","") + file.getOriginalFilename();
        filename = new DateTime().toString("yy/MM/dd") + "/" + filename;
        try {
            ossClient.putObject(bucketName, filename, file.getInputStream());
            return "https://"+ bucketName +"."+ endpoint + "/" + filename;
        } catch (Exception oe) {
            oe.printStackTrace();
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return null;
    }
}
