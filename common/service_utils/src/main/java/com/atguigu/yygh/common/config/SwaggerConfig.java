package com.atguigu.yygh.common.config;

import com.google.common.base.Predicates;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket getAdminDocket(){
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(getAdminApiInfo())
                .groupName("管理员组")
                .select()
                .paths(Predicates.and(PathSelectors.regex("/admin/.*")))
                .build();
    }

    public ApiInfo getAdminApiInfo(){
        return new ApiInfoBuilder()
                .title("尚医通预约挂号平台管理员系统")
                .contact(new Contact("","",""))
                .version("")
                .description("")
                .contact(new Contact("","",""))
                .build();
    }

    @Bean
    public Docket getUserDocket(){
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(getUserApiInfo())
                .groupName("用户组")
                .select()
                .paths(Predicates.and(PathSelectors.regex("/user/.*")))
                .build();
    }

    public ApiInfo getUserApiInfo(){
        return new ApiInfoBuilder()
                .title("尚医通预约挂号平台用户系统")
                .contact(new Contact("","",""))
                .version("")
                .description("")
                .contact(new Contact("","",""))
                .build();
    }



    @Bean
    public Docket getHospitalDocket(){
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(getHospitalApiInfo())
                .groupName("医院组")
                .select()
                .paths(Predicates.and(PathSelectors.regex("/api/.*")))
                .build();
    }

    public ApiInfo getHospitalApiInfo(){
        return new ApiInfoBuilder()
                .title("尚医通预约挂号平台医院系统")
                .contact(new Contact("","",""))
                .version("")
                .description("")
                .contact(new Contact("","",""))
                .build();
    }

}
