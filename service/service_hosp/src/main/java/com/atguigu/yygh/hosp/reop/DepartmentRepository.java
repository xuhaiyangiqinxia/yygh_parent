package com.atguigu.yygh.hosp.reop;

import com.atguigu.yygh.model.hosp.Department;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;


public interface DepartmentRepository extends MongoRepository<Department,String> {

    public Department findByHoscodeAndDepcode(String hoscode, String depcode);

    public Department getDepartmentByHoscodeAndDepcode(String hoscode,String depcode);

    List<Department> getDepartmentByHoscode(String hoscode);

}
