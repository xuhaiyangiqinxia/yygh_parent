package com.atguigu.yygh.hosp;

import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "service-hosp",path = "/user/schedule")
public interface HospFeignClient {

    @ApiOperation("通过id获取排版数据")
    @GetMapping("/inner/getScheduleOrderVo/{scheduleId}")
    public ScheduleOrderVo getScheduleOrderVo(@PathVariable String scheduleId);
}
