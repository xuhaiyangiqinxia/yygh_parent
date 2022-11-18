package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.cmn.client.DictFeignClient;
import com.atguigu.yygh.enums.DictEnum;
import com.atguigu.yygh.hosp.reop.DepartmentRepository;
import com.atguigu.yygh.hosp.reop.HospitalRepository;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.model.cmn.Dict;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class HospitalServiceImpl implements HospitalService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private DictFeignClient dictFeignClient;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public void saveHospital(HashMap<String, String> map) {
        String s = JSONObject.toJSONString(map);
        Hospital hospital = JSONObject.parseObject(s, Hospital.class);

        Query query = new Query(Criteria.where("hoscode").is(map.get("hoscode")));
        Hospital mongoHospital = mongoTemplate.findOne(query, hospital.getClass());
        if(mongoHospital == null){
            hospital.setStatus(0);
            hospital.setCreateTime(new Date());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            mongoTemplate.save(hospital);
        }else {
            hospital.setStatus(0);
            hospital.setCreateTime(mongoHospital.getCreateTime());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            //不设置id就修改不了
            hospital.setId(mongoHospital.getId());
            mongoTemplate.save(hospital);
        }
    }

    @Override
    public Hospital show(String hoscode) {
        Query query = new Query(Criteria.where("hoscode").is(hoscode));
        return mongoTemplate.findOne(query, Hospital.class);
    }

    @Override
    public Page<Hospital> findHospPage(Integer pageNum, Integer pageSize, HospitalQueryVo vo) {
        Hospital hospital = new Hospital();
        if (vo != null){
            BeanUtils.copyProperties(vo,hospital);
        }
        hospital.setIsDeleted(0);
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("hosname", ExampleMatcher.GenericPropertyMatchers.contains())
                .withIgnoreCase(true);

        Example<Hospital> example = Example.of(hospital,matcher);

        PageRequest pageAble = PageRequest.of(pageNum - 1, pageSize);
        Page<Hospital> page = hospitalRepository.findAll(example, pageAble);
        System.out.println("*******" + page.getTotalElements());
        page.stream().forEach(item -> {
            packageHospital(item);
        });
        return page;
    }

    private Hospital packageHospital(Hospital hospital){
        String province = dictFeignClient.findNameByParentDictCodeAndValue(Long.parseLong(hospital.getProvinceCode()));
        String city = dictFeignClient.findNameByParentDictCodeAndValue(Long.parseLong(hospital.getCityCode()));
        String district = dictFeignClient.findNameByParentDictCodeAndValue(Long.parseLong(hospital.getDistrictCode()));
        String type = dictFeignClient.findNameByParentDictCodeAndValue(DictEnum.HOSTYPE.getDictCode(), Long.parseLong(hospital.getHostype()));
        HashMap<String, Object> map = new HashMap<>();
        map.put("province",province);
        map.put("city",city);
        map.put("district",district);
        map.put("type",type);
        map.put("detailAddress",province+city+district);
        hospital.setParam(map);
        return hospital;
    }

    @Override
    public List<Dict> findByDictCode(String dictCode) {
        return dictFeignClient.findByDictCode(dictCode);
    }

    @Override
    public List<Dict> findDictList(Long pid) {
        return dictFeignClient.findDictList(pid);
    }

    @Override
    public void updateStatus(String id, Integer status) {
        Query query = new Query(Criteria.where("id").is(id).and("isDeleted").is(0));
        Update update = new Update();
        update.set("status",status);
        mongoTemplate.updateFirst(query,update,Hospital.class);
    }

    @Override
    public Map<String, Object> getDetails(String id) {
        Hospital hospital = packageHospital(mongoTemplate.findById(id, Hospital.class));
        HashMap<String, Object> map = new HashMap<>();
        map.put("bookingRule",hospital.getBookingRule());
        hospital.setBookingRule(null);
        map.put("hospital",hospital);
        return map;
    }

    @Override
    public List<DepartmentVo> findDeptList(String hoscode) {
        //获取所有科室列表
        List<Department> deptList = departmentRepository.getDepartmentByHoscode(hoscode);

        //创建返回结果的科室列表集合
        ArrayList<DepartmentVo> result = new ArrayList<>();
        //将科室列表按照大科室编号进行分组 key为大科室编号 value为相同大科室编号的科室列表
        Map<String, List<Department>> departmentMap = deptList.stream().collect(Collectors.groupingBy(department -> department.getBigcode()));
        //对集合进行遍历
        Set<Map.Entry<String, List<Department>>> entries = departmentMap.entrySet();
        for (Map.Entry<String, List<Department>> entry : entries) {
            //创建封装大科室的对象
            DepartmentVo departmentVo = new DepartmentVo();
            //将键中的大科室编号 和 值中的第一个科室的大科室名称 封装到一个departmentVo对象中
            departmentVo.setDepcode(entry.getKey());
            departmentVo.setDepname(entry.getValue().get(0).getBigname());
            //遍历值中的所有科室 存放到小科室列表中
            ArrayList<DepartmentVo> smallDepartmentVoList = new ArrayList<>();
            for (Department department : entry.getValue()) {
                DepartmentVo smallDepartmentVo = new DepartmentVo();
                BeanUtils.copyProperties(department,smallDepartmentVo);
                smallDepartmentVoList.add(smallDepartmentVo);
            }
            //将小科室列表封装到大科室中
            departmentVo.setChildren(smallDepartmentVoList);
            //将大科室封装到结果集中
            result.add(departmentVo);
        }
        return result;
    }

    public String getHosname(String hoscode){
        Hospital hospital = new Hospital();
        hospital.setHoscode(hoscode);
        Query query = new Query(Criteria.where("hoscode").is(hoscode));
        List<Hospital> hospitals = mongoTemplate.find(query, Hospital.class);
        if (hospitals.size() > 0) {
            return hospitals.get(0).getHosname();
        }else return null;
    }

    @Override
    public List<Hospital> findByHosname(String hosname) {
        return hospitalRepository.findHospitalByHosnameLike(hosname);
    }

    @Override
    public Map<String, Object> item(String hoscode) {
            Query query = new Query(Criteria.where("hoscode").is(hoscode));
            Hospital hospital = packageHospital(mongoTemplate.findOne(query,Hospital.class));
            HashMap<String, Object> map = new HashMap<>();
            map.put("bookingRule",hospital.getBookingRule());
            hospital.setBookingRule(null);
            map.put("hospital",hospital);
            return map;

    }
}
