package com.atguigu.yygh.hosp.test;

import com.atguigu.yygh.hosp.bean.User;
import com.atguigu.yygh.hosp.reop.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SpringBootTest
public class MongoRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testMongoRepository(){
//        User user = new User(null, "黄渤", true, 29);
//        userRepository.insert(user);

        ArrayList<User> users = new ArrayList<>();
        users.add(new User(null,"Andy",false,20));
        users.add(new User(null,"angle",false,15));
        users.add(new User(null,"lame",false,36));
        users.add(new User(null,"sum",false,30));
        userRepository.insert(users);

        List<User> all = userRepository.findAll();
        System.out.println("all = " + all);
    }

    @Test
    public void testDelete(){
        //userRepository.deleteById("636861e663c5800542cce480");
        User user = new User();
        user.setId("636861e663c5800542cce47f");
        userRepository.delete(user);
    }

    @Test
    public void testQuery(){
        /*User user = new User();
        user.setAge(20);
        Example<User> example = Example.of(user);
        Optional<User> one = userRepository.findOne(example);
        System.out.println("one = " + one);*/

        User user = new User(null, null, false, null);
        Example<User> example = Example.of(user);
        List<User> all = userRepository.findAll(example);
        for (User user1 : all) {
            System.out.println("user1 = " + user1);
        }
    }

    @Test
    public void testLikeQuery(){
//        ExampleMatcher matcher = ExampleMatcher.matching()
//                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

        ExampleMatcher matcher = ExampleMatcher.matching()
//                .withMatcher("name", ExampleMatcher.GenericPropertyMatchers.contains())
                .withMatcher("name",ExampleMatcher.GenericPropertyMatchers.startsWith())
                .withIgnoreCase(true);
        User user = new User();
        user.setName("a");
        Example<User> example = Example.of(user, matcher);
        List<User> all = userRepository.findAll(example);
        System.out.println("all = " + all);
    }

    @Test
    public void testPage(){
        int pageNum = 1;
        int pageSize = 2;
        User user = new User();
        user.setGender(false);
        Example<User> example = Example.of(user);
        Pageable pageRequest = PageRequest.of(pageNum - 1, pageSize);
        Page<User> all = userRepository.findAll(example, pageRequest);
        System.out.println("all.getTotalPages() = " + all.getTotalPages());
        System.out.println("all.getTotalElements() = " + all.getTotalElements());
        System.out.println("all.getContent() = " + all.getContent());
    }

    @Test
    public void testSelfDefine(){
        List<User> list = userRepository.findByGenderFalseAndName("Andy");
        System.out.println("list = " + list);
    }
}
