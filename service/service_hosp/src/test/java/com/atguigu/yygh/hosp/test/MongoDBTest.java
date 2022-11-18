package com.atguigu.yygh.hosp.test;

import com.atguigu.yygh.hosp.bean.User;
import com.atguigu.yygh.model.hosp.Hospital;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class MongoDBTest {
    @Autowired
    private MongoTemplate mongoTemplate;


    @Test
    public void testInsert(){
        User user = new User("1", "赵院长", false, 29);
        user = mongoTemplate.insert(user);
        System.out.println(user);
    }

    @Test
    public void testFind(){
        List<User> all = mongoTemplate.findAll(User.class);
        System.out.println("all = " + all);
    }

    @Test
    public void testBatchInsert(){
        List<User> users = Arrays.asList(new User("2", "zhangsan", true, 24),
                new User("3", "lisi", true, 24),
                new User("4", "wangwu", true, 24),
                new User("5", "zhaoliu", true, 24));
        mongoTemplate.insert(users,User.class);
    }

    @Test
    public void testRemove(){
        Criteria criteria = new Criteria();
        criteria.orOperator(Criteria.where("id").is("2"),Criteria.where("id").is("3"));
        Query query = new Query(criteria);
        mongoTemplate.remove(query, User.class);
    }

    @Test
    public void testUpdate(){
        Query query = new Query(Criteria.where("id").is("6368ee1bb987e75456a3f133"));
        Update update = new Update();
        update.set("name","几块几十块");
        mongoTemplate.upsert(query,update,User.class);
    }

    @Test
    public void testUpdate1(){
        Update update = new Update();
        Update set = update.set("name", "赵小子");
        Query query = new Query(Criteria.where("name").is("赵院长"));
        mongoTemplate.updateMulti(query,update, User.class);
    }

    @Test
    public void testUpdate2(){
        Update update = new Update();
        Update set = update.set("status",1);
        Query query = new Query(Criteria.where("id").is("636909e078ca744e146d788f"));
        mongoTemplate.updateFirst(query,update, Hospital.class);
    }

    @Test
    public void testFind2(){
        String str = String.format("%s%s%s", ".*", "赵", ".*");
        Query query = new Query(Criteria.where("name").regex(str));
        List<User> users = mongoTemplate.find(query, User.class);
        System.out.println(users);
    }
}


