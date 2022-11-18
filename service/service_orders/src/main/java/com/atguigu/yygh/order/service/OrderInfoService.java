package com.atguigu.yygh.order.service;

import com.atguigu.yygh.model.order.OrderInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author XuSir
 * @since 2022-11-17
 */
public interface OrderInfoService extends IService<OrderInfo> {

    void saveOrder(String scheduleId, Long patientId);
}
