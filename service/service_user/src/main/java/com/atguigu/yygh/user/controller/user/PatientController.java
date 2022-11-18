package com.atguigu.yygh.user.controller.user;


import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.common.utils.JwtHelper;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.user.service.PatientService;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 就诊人表 前端控制器
 * </p>
 *
 * @author XuSir
 * @since 2022-11-15
 */
@Api(tags = "就诊人管理")
@RestController
@RequestMapping("/user/patient")
@Slf4j
public class PatientController {

    @Autowired
    private PatientService patientService;

    @ApiOperation(value = "添加就诊人信息")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "patient", value = "就诊人实体类", required = true),
            @ApiImplicitParam(name = "token", value = "前端的token保存了当前登陆用户的id和name")
    })
    @PostMapping("/save")
    public R save(@RequestBody Patient patient , @RequestHeader String token){
        patient.setUserId(JwtHelper.getUserId(token));
        patientService.save(patient);
        return R.ok();
    }


    @ApiOperation("删除就诊人信息")
    @DeleteMapping("/remove/{id}")
    public R remove(@PathVariable Long id){
        patientService.removeById(id);
        return R.ok();
    }

    @ApiOperation("编辑就诊人信息")
    @PutMapping("/update")
    public R edit(@RequestBody Patient patient){
        patientService.updateById(patient);
        return R.ok();
    }

    @ApiOperation("获取所有就诊人信息")
    @GetMapping("/all")
    public R all(@RequestHeader String token){
        List<Patient> list = patientService.all(JwtHelper.getUserId(token));
        return R.ok().data("list",list);
    }

    @ApiOperation("通过id获取就诊人信息")
    @GetMapping("/get/{id}")
    public R getPatient(@PathVariable Long id){
        Patient patient = patientService.getPatientById(id);
        return R.ok().data("patient",patient);
    }

    @ApiOperation(value = "获取就诊人")
    @GetMapping("inner/get/{id}")
    public Patient getPatientOrder(
            @ApiParam(name = "id", value = "就诊人id", required = true)
            @PathVariable("id") Long id) {
        return patientService.getById(id);
    }

}

