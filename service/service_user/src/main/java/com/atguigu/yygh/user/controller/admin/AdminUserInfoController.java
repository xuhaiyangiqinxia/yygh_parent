package com.atguigu.yygh.user.controller.admin;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/userInfo")
@Slf4j
public class AdminUserInfoController {
    @Autowired
    private UserInfoService userInfoService;

    //用户列表（条件查询带分页）
    @GetMapping("/{page}/{limit}")
    public R list(@PathVariable Long page,
                  @PathVariable Long limit,
                  String userInfoQueryVo) {
        UserInfoQueryVo userInfoQueryVo1 = JSONObject.parseObject(userInfoQueryVo, UserInfoQueryVo.class);
        log.info(userInfoQueryVo1.getKeyword());
        Page<UserInfo> pageParam = new Page<>(page,limit);
        IPage<UserInfo> pageModel = userInfoService.selectPage(pageParam,userInfoQueryVo1);
        return R.ok().data("pageModel",pageModel);
    }

    @PutMapping("/{id}/{status}")
    public R lock(@PathVariable Long id, @PathVariable Integer status){
        UserInfo userInfo = new UserInfo();
        userInfo.setId(id);
        userInfo.setStatus(status);
        userInfoService.saveOrUpdate(userInfo);
        return R.ok();
    }

    @GetMapping("/show/{id}")
    public R show(@PathVariable Long id){
        Map<String,Object> map =  userInfoService.show(id);
        return R.ok().data(map);
    }

    @GetMapping("/approval/{id}/{authStatus}")
    public R approval(@PathVariable Long id, @PathVariable Integer authStatus){
        System.out.println(authStatus);
        userInfoService.approval(id,authStatus);
        return R.ok();
    }
}
