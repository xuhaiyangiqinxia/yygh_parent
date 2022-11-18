package com.atguigu.yygh.hosp.controller.admin;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Schedule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/schedule")
@Slf4j
public class AdminScheduleController {


    @Autowired
    private ScheduleService scheduleService;

    @GetMapping("/page/{pageNum}/{pageSize}/{hoscode}/{depcode}")
    public R page(@PathVariable Integer pageNum,
                  @PathVariable Integer pageSize,
                  @PathVariable String hoscode,
                  @PathVariable String depcode
                  ){
        Map<String, Object> map = scheduleService.page(pageNum,pageSize,hoscode,depcode);
        //log.info(pageNum + "*" + pageSize + "*" + hoscode + "*" + depcode);
        return R.ok().data(map);
    }

    @GetMapping("/detailSchedule/{hoscode}/{depcode}")
    public R findDetailSchedule(@PathVariable String hoscode, @PathVariable String depcode, @RequestParam(value = "workDate") String workDate){
        //log.info(hoscode + "===" + depcode + "===" + workDate);
        List<Schedule> schedulesList = scheduleService.getDetailSchedule(hoscode,depcode,workDate);
        log.info(schedulesList.toString());
        return R.ok().data("scheduleList",schedulesList);
    }
}
