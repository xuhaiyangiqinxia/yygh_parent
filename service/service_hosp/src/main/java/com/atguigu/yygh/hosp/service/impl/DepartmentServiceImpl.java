package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.hosp.reop.DepartmentRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private HospitalService hospitalService;


    @Override
    public void saveDepartment(Map<String, Object> map) {
        Department department = departmentRepository.findByHoscodeAndDepcode((String) map.get("hoscode"), (String) map.get("depcode"));

        if(department == null){
            department = JSONObject.parseObject(JSONObject.toJSONString(map), Department.class);
            department.setCreateTime(new Date());
            department.setUpdateTime(new Date());
            department.setIsDeleted(0);
            departmentRepository.save(department);
        }else {
            department.setCreateTime(department.getUpdateTime());
            department.setUpdateTime(new Date());
            department.setIsDeleted(0);
            department.setId(department.getId());
            departmentRepository.save(department);
        }
    }

    @Override
    public Map<String, Object> findDepartment(Map<String, String> map) {
        //设置查询条件
        Department department = new Department();
        department.setHoscode(map.get("hoscode"));
        department.setIsDeleted(0);
        Example<Department> example = Example.of(department);
        //设置分页条件
        PageRequest page = PageRequest.of(Integer.parseInt(map.get("pageNum")) - 1, Integer.parseInt(map.get("pageSize")));
        Page<Department> list = departmentRepository.findAll(example, page);
        HashMap<String, Object> result = new HashMap<>();
        result.put("totalElements",list.getTotalElements());
        result.put("content",list.getContent());
        return result;
    }

    @Override
    public void removeDepartment(Map<String, String> map) {
        //验证SignKey...

        //删除科室
        Department department = departmentRepository.getDepartmentByHoscodeAndDepcode(map.get("hoscode"), map.get("depcode"));
        department.setIsDeleted(1);
        departmentRepository.save(department);
    }

    @Override
    public String getDepname(String depcode) {
        Department department = new Department();
        department.setDepcode(depcode);
        Query query = new Query(Criteria.where("depcode").is(depcode));
        Example<Department> example = Example.of(department);
        List<Department> departments = departmentRepository.findAll(example);
        if (departments.size() > 0) {
            return departments.get(0).getDepname();
        }
        return null;
    }

    @Override
    public List<DepartmentVo> findDeptTree(String hoscode) {
        return hospitalService.findDeptList(hoscode);
    }

    @Override
    public Department getDepartment(String hoscode, String depcode) {
        Department department = new Department();
        department.setHoscode(hoscode);
        department.setDepcode(depcode);
        Example<Department> example = Example.of(department);
        Optional<Department> optional = departmentRepository.findOne(example);
        if(optional == null){
            throw  new YyghException(2005,"医院或科室有误");
        }
        Department department1 = optional.get();
        return department1;
    }
}
