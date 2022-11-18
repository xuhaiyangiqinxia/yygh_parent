package com.atguigu.yygh.hosp.reop;

import com.atguigu.yygh.hosp.bean.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserRepository extends MongoRepository<User,String> {

    public List<User> findByGenderFalseAndName(String name);

}
