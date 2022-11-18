package com.atguigu.yygh.hosp.reop;

import com.atguigu.yygh.model.hosp.Hospital;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;


public interface HospitalRepository extends MongoRepository<Hospital,String> {

    List<Hospital> findHospitalByHosnameLike(String hosname);
}
