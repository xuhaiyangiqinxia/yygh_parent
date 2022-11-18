package com.atguigu.yygh.hosp.controller.api;

import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.impl.HospitalSetServiceImpl;
import com.atguigu.yygh.hosp.utils.Result;
import com.atguigu.yygh.model.hosp.Hospital;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/hosp")
public class ApiHospitalController {

    @Autowired
    private HospitalSetServiceImpl hospitalSetService;

    @Autowired
    private HospitalService hospitalService;

    @PostMapping("/saveHospital")
    /*
        接收传过来的json数据四种方式:
        1.参数位置放一个HttpRequest,使用request.getParameter
        2.一个一个参数接收,参数名与json中的键对应
        3.springMVC中接收多个参数可以使用pojo类接收，不在pojo类中的在多加几个参数单独接收
        4.参数位置添加@RequestParam注解，使用一个map集合接收
     */
    public Result saveHospital(@RequestParam HashMap<String,String> map){
        String logoData = map.get("logoData");
        String s = logoData.replaceAll(" ", "+");
        map.put("logoData",s);
        System.out.println("map = " + map);

        //获取医院的signkey
        String signKey = map.get("sign");
        //获取医院编号
        String hoscode = map.get("hoscode");
        //从数据库通过医院编号查询signkey
        String platFormSignKey = hospitalSetService.getSignKeyByHoscode(hoscode);
        if(StringUtils.isEmpty(platFormSignKey) || StringUtils.isEmpty(signKey) || !platFormSignKey.equals(signKey)){
            throw new YyghException(201,"signKey有误");
        }
        hospitalService.saveHospital(map);
        return Result.ok();
    }

    @PostMapping("/hospital/show")
    public Result show(@RequestParam Map<String,String> map){
        String hoscode = map.get("hoscode");
        Hospital hospital = hospitalService.show(hoscode);
        return Result.ok(hospital);
    }

}
