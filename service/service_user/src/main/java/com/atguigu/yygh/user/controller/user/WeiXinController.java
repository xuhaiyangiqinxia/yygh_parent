package com.atguigu.yygh.user.controller.user;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.common.utils.JwtHelper;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.prop.WeixinProperties;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.user.utils.HttpClientUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/user/wx")
public class WeiXinController {

    @Autowired
    private WeixinProperties weixinProperties;

    @Autowired
    private UserInfoService userInfoService;

    @GetMapping("params")
    @ResponseBody
    public R login() throws UnsupportedEncodingException {
        Map<String,Object> map = new HashMap<>();
        map.put("appid",weixinProperties.getAppid());
        map.put("scope","snsapi_login");
        map.put("redirect_uri", URLEncoder.encode(weixinProperties.getRedirecturl(),"UTF-8"));
        map.put("state",System.currentTimeMillis()+"");
        return R.ok().data(map);
    }

    //https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
    @GetMapping("/callback")
    public String callback(String code,String state) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder append = stringBuilder.append("https://api.weixin.qq.com/sns/oauth2/access_token?")
                .append("appid=%s")
                .append("&secret=%s")
                .append("&code=%s")
                .append("&grant_type=authorization_code");
        String format = String.format(append.toString(), weixinProperties.getAppid(), weixinProperties.getAppsecret(), code);
        String s = HttpClientUtils.get(format);

        Map map = JSONObject.parseObject(s, Map.class);
        String access_token = map.get("access_token").toString();
        String openid = map.get("openid").toString();

        System.out.println(access_token + "*****" + openid);
        UserInfo userInfo = userInfoService.queryUserByOpenId(openid);
        System.out.println(userInfo);
        if(userInfo == null){
            userInfo = new UserInfo();
            userInfo.setOpenid(openid);

            //获取微信用户信息
            //https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID
            StringBuilder stringBuilder2 = new StringBuilder();
            StringBuilder append2 = stringBuilder2.append("https://api.weixin.qq.com/sns/userinfo?")
                    .append("access_token=%s")
                    .append("&openid=%s");
            String format2 = String.format(append2.toString(), access_token, openid);
            String s2 = HttpClientUtils.get(format2);
            JSONObject jsonObject = JSONObject.parseObject(s2);
            //获取用户昵称
            userInfo.setNickName(jsonObject.getString("nickname"));
            userInfo.setStatus(1);
            System.out.println(userInfo.getNickName());

            userInfoService.save(userInfo);
        }

        if(userInfo.getStatus() == 0){
            throw new YyghException(2002,"该用户已经被禁用");
        }

        HashMap<String, String> resultMap = new HashMap<>();
        String name = userInfo.getName();
        if(StringUtils.isEmpty(name)){
            name = userInfo.getNickName();
        }
        if(StringUtils.isEmpty(name)){
            name = userInfo.getPhone();
        }
        resultMap.put("name",name);
        String token = JwtHelper.createToken(userInfo.getId(), name);
        resultMap.put("token",token);

        if(StringUtils.isEmpty(userInfo.getPhone())){
            resultMap.put("openid",openid);
        }else {
            resultMap.put("openid","");
        }
        return "redirect:http://localhost:3000/weixin/callback?token="+resultMap.get("token")+ "&openid="+resultMap.get("openid")+"&name="+URLEncoder.encode(resultMap.get("name"),"utf-8");
    }
}
