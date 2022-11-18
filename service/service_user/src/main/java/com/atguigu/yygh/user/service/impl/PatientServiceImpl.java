package com.atguigu.yygh.user.service.impl;

import com.atguigu.yygh.cmn.client.DictFeignClient;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.user.mapper.PatientMapper;
import com.atguigu.yygh.user.service.PatientService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 就诊人表 服务实现类
 * </p>
 *
 * @author XuSir
 * @since 2022-11-15
 */
@Service
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient> implements PatientService {

    @Autowired
    private DictFeignClient dictFeignClient;
    @Override
    public List<Patient> all(Long userId) {
        QueryWrapper<Patient> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        List<Patient> patients = baseMapper.selectList(wrapper);
        patients.forEach(item -> {
            item = packagePatient(item);
        });
        return patients;
    }

    @Override
    public Patient getPatientById(Long id) {
        Patient patient = baseMapper.selectById(id);
        return packagePatient(patient);
    }

    private Patient packagePatient(Patient patient){
        String certificatesTypeString = dictFeignClient.findNameByParentDictCodeAndValue(Long.parseLong(patient.getCertificatesType()));
        String provinceString = dictFeignClient.findNameByParentDictCodeAndValue(Long.parseLong(patient.getProvinceCode()));
        String cityString = dictFeignClient.findNameByParentDictCodeAndValue(Long.parseLong(patient.getCityCode()));
        String districtString = dictFeignClient.findNameByParentDictCodeAndValue(Long.parseLong(patient.getDistrictCode()));
        patient.getParam().put("provinceString",provinceString);
        patient.getParam().put("certificatesTypeString",certificatesTypeString);
        patient.getParam().put("cityString",cityString);
        patient.getParam().put("districtString",districtString);
        String fullAddress = provinceString + cityString + districtString + patient.getAddress();
        patient.getParam().put("fullAddress",fullAddress);
        return patient;
    }
}

