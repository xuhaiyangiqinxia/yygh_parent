package com.atguigu.yygh.order.controller;


import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.order.service.OrderInfoService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 订单表 前端控制器
 * </p>
 *
 * @author XuSir
 * @since 2022-11-17
 */
@Api(tags = "订单接口")
@RestController
@RequestMapping("/user/order/orderInfo")
public class OrderInfoController {

    @Autowired
    private OrderInfoService orderInfoService;

    @PostMapping("/auth/submitOrder/{scheduleId}/{patientId}")
    public R submitOrder(@PathVariable String scheduleId, @PathVariable Long patientId){
        orderInfoService.saveOrder(scheduleId,patientId);
        return R.ok();
    }

}

