package com.atguigu.yygh.user.service.impl;

import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.utils.JwtHelper;
import com.atguigu.yygh.enums.AuthStatusEnum;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.mapper.UserInfoMapper;
import com.atguigu.yygh.user.service.PatientService;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.vo.user.LoginVo;
import com.atguigu.yygh.vo.user.UserAuthVo;
import com.atguigu.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author XuSir
 * @since 2022-11-12
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private PatientService patientService;

    @Override
    public Map<String, Object> login(LoginVo loginVo) {
        //1。获取手机号和验证码
        String phone = loginVo.getPhone();
        String code = loginVo.getCode();
        //2.惊醒手机号和验证码的非空验证
        if(StringUtils.isEmpty(phone) || StringUtils.isEmpty(code)){
            throw new YyghException(2001,"手机号或验证码不能为空");
        }
        //3.ToDo:对验证码进行验证
        String redisCode = redisTemplate.opsForValue().get(phone);
        if(StringUtils.isEmpty(redisCode) || !code.equals(redisCode)){
            throw new YyghException(2001,"验证码有误");
        }

        UserInfo userInfo = null;
        //通过是否携带openid判断用户有没有通过微信登陆过
        //用户未扫码登录
        System.out.println(loginVo.getOpenid());
        if(StringUtils.isEmpty(loginVo.getOpenid())){
            //4.根据用户的手机号进行查询，判断用户是不是首次登陆
            QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
            wrapper.eq("phone",phone);
            userInfo = baseMapper.selectOne(wrapper);
            //4.1如果是首次登陆，先注册用户信息
            if (userInfo == null){
                userInfo = new UserInfo();
                userInfo.setPhone(phone);
                userInfo.setStatus(1);
                baseMapper.insert(userInfo);
            }
            //4.2如果不是首次登陆，往下走

        //用户已经扫码登陆过
        }else {
            //通过手机号先查询该手机号是否注册
            QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
            wrapper.eq("phone",phone);
            userInfo = baseMapper.selectOne(wrapper);
            //通过openid获取微信登录的信息
            QueryWrapper<UserInfo> wrapper2 = new QueryWrapper<>();
            wrapper2.eq("openid",loginVo.getOpenid());
            UserInfo userInfo2 = baseMapper.selectOne(wrapper2);


            if (userInfo == null){
                //该手机号未注册
                userInfo = new UserInfo();
                userInfo.setStatus(1);
                userInfo.setPhone(phone);
            }
            userInfo.setNickName(userInfo2.getNickName());
            userInfo.setOpenid(userInfo2.getOpenid());
            //删除原微信账户
            baseMapper.deleteById(userInfo2.getId());
            baseMapper.updateById(userInfo);
        }


        //5.对用户状态进行判断，如果被禁用抛出异常
        if(userInfo.getStatus() == 0){
            throw new YyghException(20001,"该用户已经被禁用");
        }
        //6.返回用户信息
        HashMap<String, Object> map = new HashMap<>();
        String name = userInfo.getName();
        if(StringUtils.isEmpty(name)){
            name = userInfo.getNickName();
        }
        if(StringUtils.isEmpty(name)){
            name = userInfo.getPhone();
        }
        map.put("name",name);
        String token = JwtHelper.createToken(userInfo.getId(), name);
        map.put("token",token);
        return map;
    }

    @Override
    public UserInfo queryUserByOpenId(String oppenid) {
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("openid",oppenid);
        UserInfo userInfo = baseMapper.selectOne(wrapper);
        return userInfo;
    }

    @Override
    public void userAuth(Long userId, UserAuthVo userAuthVo) {
        //通过用户id查询用户信息
        UserInfo userInfo = baseMapper.selectById(userId);
        //设置用户信息
        userInfo.setName(userAuthVo.getName());
        userInfo.setCertificatesType(userAuthVo.getCertificatesType());
        userInfo.setCertificatesNo(userAuthVo.getCertificatesNo());
        userInfo.setCertificatesUrl(userAuthVo.getCertificatesUrl());
        userInfo.setAuthStatus(AuthStatusEnum.AUTH_RUN.getStatus());
        //更新用户信息
        baseMapper.updateById(userInfo);
    }

    @Override
    public UserInfo getUserInfo(Long userId){
        //查询用户信息 获取认证状态
        UserInfo userInfo = baseMapper.selectById(userId);
        Integer authStatus = userInfo.getAuthStatus();
        //获取认证状态名称
        String statusName = AuthStatusEnum.getStatusNameByStatus(authStatus);
        HashMap<String, Object> map = new HashMap<>();
        map.put("authStatusString",statusName);
        userInfo.setParam(map);
        return userInfo;
    }

    @Override
    public IPage<UserInfo> selectPage(Page<UserInfo> pageParam, UserInfoQueryVo userInfoQueryVo) {
        //UserInfoQueryVo获取条件值
        String name = userInfoQueryVo.getKeyword(); //用户名称
        Integer status = userInfoQueryVo.getStatus();//用户状态
        Integer authStatus = userInfoQueryVo.getAuthStatus(); //认证状态
        String createTimeBegin = userInfoQueryVo.getCreateTimeBegin(); //开始时间
        String createTimeEnd = userInfoQueryVo.getCreateTimeEnd(); //结束时间
        //对条件值进行非空判断
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        if(!StringUtils.isEmpty(status)) {
            wrapper.eq("status",status);
        }
        if(!StringUtils.isEmpty(authStatus)) {
            wrapper.eq("auth_status",authStatus);
        }
        if(!StringUtils.isEmpty(createTimeBegin)) {
            wrapper.ge("create_time",createTimeBegin);
        }
        if(!StringUtils.isEmpty(createTimeEnd)) {
            wrapper.le("create_time",createTimeEnd);
        }
        if(!StringUtils.isEmpty(name)) {
            wrapper.and(item -> {
                item.like("name",name).or().eq("phone",name);
            });
        }

        //调用mapper的方法
        //调用mapper的方法
        IPage<UserInfo> pages = baseMapper.selectPage(pageParam, wrapper);
        //编号变成对应值封装
        pages.getRecords().stream().forEach(item -> {
            this.packageUserInfo(item);
        });
        return pages;
    }

    //编号变成对应值封装
    private UserInfo packageUserInfo(UserInfo userInfo) {
        //处理认证状态编码
        userInfo.getParam().put("authStatusString",AuthStatusEnum.getStatusNameByStatus(userInfo.getAuthStatus()));
        //处理用户状态 0  1
        String statusString = userInfo.getStatus().intValue()==0 ?"锁定" : "正常";
        userInfo.getParam().put("statusString",statusString);
        return userInfo;
    }

    @Override
    public Map<String, Object> show(Long id) {
        UserInfo userInfo = baseMapper.selectById(id);
        HashMap<String, Object> map = new HashMap<>();
        map.put("userInfo",userInfo);
        List<Patient> patientList = patientService.all(id);
        map.put("patientList",patientList);
        return map;
    }

    @Override
    public void approval(Long id, Integer authStatus) {
        if(authStatus != 2 && authStatus != -1){
            throw new YyghException(2004,"认证异常");
        }
        UserInfo userInfo = baseMapper.selectById(id);
        userInfo.setAuthStatus(authStatus);
        baseMapper.updateById(userInfo);
    }
}
