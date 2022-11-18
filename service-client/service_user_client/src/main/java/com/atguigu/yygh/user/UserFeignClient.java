package com.atguigu.yygh.user;

import com.atguigu.yygh.model.user.Patient;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "service-user",path = "/user/patient")
public interface UserFeignClient {

    @ApiOperation(value = "获取就诊人")
    @GetMapping("inner/get/{id}")
    public Patient getPatientOrder(@ApiParam(name = "id", value = "就诊人id", required = true) @PathVariable("id") Long id);
}
