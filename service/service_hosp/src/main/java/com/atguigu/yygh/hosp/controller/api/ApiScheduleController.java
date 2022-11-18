package com.atguigu.yygh.hosp.controller.api;

import com.atguigu.yygh.hosp.service.ScheduleService;
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

@RestController
@RequestMapping("/api/hosp")
@Slf4j
public class ApiScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @PostMapping("/saveSchedule")
    public Result saveSchedule(HttpServletRequest request){
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());
        scheduleService.saveSchedule(map);
        return Result.ok();
    }

    @PostMapping("/schedule/list")
    public Result findSchedulePage(@RequestParam Map<String,String> map){
        Map<String,Object> result = scheduleService.findSchedulePage(map);
        log.info("结果：===========》" + result);
        return Result.ok(result);
    }

    @PostMapping("/schedule/remove")
    public Result removeSchedule(@RequestParam Map<String,String> map){
        scheduleService.removeSchedule(map);
        return Result.ok();
    }

}
