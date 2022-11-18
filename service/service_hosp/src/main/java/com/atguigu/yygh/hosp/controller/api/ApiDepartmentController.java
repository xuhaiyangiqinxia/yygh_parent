package com.atguigu.yygh.hosp.controller.api;

import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.utils.HttpRequestHelper;
import com.atguigu.yygh.hosp.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RequestMapping("/api/hosp")
@RestController
@Slf4j
public class ApiDepartmentController {

    @Autowired
    private DepartmentService departmentService;


    @PostMapping("/saveDepartment")
    public Result saveDepartment(HttpServletRequest request){

        //验证signkey.......

        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);
        departmentService.saveDepartment(map);
        return Result.ok();
    }

    @PostMapping("/department/list")
    public Result findDepartment(@RequestParam Map<String,String> map){

        //验证signkey...

        Map<String, Object> result = departmentService.findDepartment(map);
        return Result.ok(result);
    }

    @PostMapping("/department/remove")
    public Result removeDepartment(@RequestParam Map<String,String> map){
        departmentService.removeDepartment(map);
        return Result.ok();
    }

}
