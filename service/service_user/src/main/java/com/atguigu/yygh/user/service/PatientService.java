package com.atguigu.yygh.user.service;

import com.atguigu.yygh.model.user.Patient;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 就诊人表 服务类
 * </p>
 *
 * @author XuSir
 * @since 2022-11-15
 */
public interface PatientService extends IService<Patient> {

    List<Patient> all(Long userId);

    Patient getPatientById(Long id);
}
