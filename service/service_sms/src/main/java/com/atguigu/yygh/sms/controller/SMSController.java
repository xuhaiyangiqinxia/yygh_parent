package com.atguigu.yygh.sms.controller;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.sms.service.SMSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/sms")
public class SMSController {

    @Autowired
    private SMSService smsService;


    @GetMapping(value = "/send/{phone}")
    public R code(@PathVariable String phone) {
        boolean isSend = smsService.send(phone);
        if(isSend){
            return R.ok();
        }else {
            return R.error();
        }
    }
}
