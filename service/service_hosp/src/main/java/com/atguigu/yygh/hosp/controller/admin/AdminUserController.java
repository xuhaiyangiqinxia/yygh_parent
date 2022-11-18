package com.atguigu.yygh.hosp.controller.admin;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.model.acl.User;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/admin/user")
public class AdminUserController {

    @PostMapping("/login")
    public R login(@RequestBody User user){
        //System.out.println(user);
        return R.ok().data("token","admin-token");
    }

    @GetMapping("/info")
    public R getInfo(@RequestParam String token){
        //System.out.println(token);
        HashMap<String, Object> map = new HashMap<>();
        map.put("roles", "[admin]");
        map.put("introduction" , "I am a super administrator");
        map.put("avatar" , "https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif");
        map.put("name" , "Super Admin");
        return R.ok().data(map);
    }
}
