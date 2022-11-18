package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.vo.hosp.DepartmentVo;

import java.util.List;
import java.util.Map;

public interface DepartmentService {

    void saveDepartment(Map<String, Object> map);

    Map<String, Object> findDepartment(Map<String, String> map);

    void removeDepartment(Map<String, String> map);

    String getDepname(String depcode);

    List<DepartmentVo> findDeptTree(String hoscode);

    Department getDepartment(String hoscode, String depcode);
}
