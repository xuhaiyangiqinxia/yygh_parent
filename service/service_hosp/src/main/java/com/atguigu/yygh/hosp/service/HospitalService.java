package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.cmn.Dict;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.data.domain.Page;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface HospitalService{
    void saveHospital(HashMap<String, String> map);

    Hospital show(String hoscode);

    Page<Hospital> findHospPage(Integer pageNum, Integer pageSize, HospitalQueryVo vo);

    List<Dict> findByDictCode(String dictCode);

    List<Dict> findDictList(Long pid);

    void updateStatus(String id, Integer status);

    Map<String,Object> getDetails(String id);

    List<DepartmentVo> findDeptList(String hoscode);

    String getHosname(String hoscode);

    List<Hospital> findByHosname(String hosname);

    Map<String, Object> item(String hoscode);

}
