package com.atguigu.yygh.common.result;

import io.swagger.models.auth.In;
import lombok.Getter;

@Getter
public enum REnum {
    SUCCESS(20000,true,"成功"),
    ERROR(20001,false,"失败")
    ;

    private Integer code;
    private Boolean success;
    private String message;

    REnum(Integer code,Boolean success,String message){
        this.code = code;
        this.success = success;
        this.message = message;
    }

}
