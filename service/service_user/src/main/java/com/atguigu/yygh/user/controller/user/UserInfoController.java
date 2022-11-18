package com.atguigu.yygh.user.controller.user;


import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.common.utils.JwtHelper;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.vo.user.LoginVo;
import com.atguigu.yygh.vo.user.UserAuthVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author XuSir
 * @since 2022-11-12
 */
@RestController
@RequestMapping("/user/userinfo")
public class UserInfoController {

    @Autowired
    private UserInfoService userInfoService;

    @PostMapping("/login")
    public R login(@RequestBody LoginVo loginVo){
        Map<String,Object> result = userInfoService.login(loginVo);
        return R.ok().data(result);
    }

    @GetMapping("/auth/getUserInfo")
    public R getUserInfo(@RequestHeader String token){
        Long userId = JwtHelper.getUserId(token);
        System.out.println(userId);
        UserInfo user = userInfoService.getUserInfo(userId);
        return R.ok().data("userInfo",user);
    }

    @PostMapping("/auth/userAuth")
    public R sava(@RequestHeader String token ,@RequestBody UserAuthVo userAuthVo){
        Long userId = JwtHelper.getUserId(token);
        userInfoService.userAuth(userId,userAuthVo);
        return R.ok();
    }

}

