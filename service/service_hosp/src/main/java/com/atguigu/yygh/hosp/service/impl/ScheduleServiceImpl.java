package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.hosp.reop.ScheduleRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.BookingRule;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.BookingScheduleRuleVo;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private DepartmentService departmentService;


    @Override
    public Schedule getScheduleById(String scheduleId) {
        Optional<Schedule> optional = scheduleRepository.findById(scheduleId);
        if(optional == null){
            throw new YyghException(2006,"排版id异常");
        }
        Schedule schedule = optional.get();
        schedule = packageSchedule(schedule);
        return schedule;
    }

    @Override
    public ScheduleOrderVo getScheduleOrderVo(String scheduleId) {
        Schedule schedule = mongoTemplate.findById(scheduleId, Schedule.class);
        log.info("schedule: " + schedule);
        ScheduleOrderVo scheduleOrderVo = packageScheduleOrderVo(schedule);
        log.info("scheduleOrderVo: " + scheduleOrderVo);
        return scheduleOrderVo;
    }


    private ScheduleOrderVo packageScheduleOrderVo(Schedule schedule){
        //获取排班信息
        if(schedule == null){
            throw new YyghException(2003,"排班信息转换异常");
        }
        String hoscode = schedule.getHoscode();
        Hospital hospital = hospitalService.show(hoscode);
        if(hospital ==null){
            throw new YyghException(2003,"排班信息转换异常");
        }
        BookingRule bookingRule = hospital.getBookingRule();
        //为ScheduleOrderVo赋值
        ScheduleOrderVo scheduleOrderVo = new ScheduleOrderVo();
        scheduleOrderVo.setDepcode(schedule.getDepcode());
        scheduleOrderVo.setHoscode(schedule.getHoscode());
        scheduleOrderVo.setHosname(departmentService.getDepname(schedule.getDepcode()));
        scheduleOrderVo.setAmount(schedule.getAmount());
        scheduleOrderVo.setAvailableNumber(schedule.getAvailableNumber());
        scheduleOrderVo.setHosScheduleId(schedule.getHosScheduleId());
        //获取退号截止时间
        DateTime quitDay = new DateTime(schedule.getWorkDate()).plusDays(bookingRule.getQuitDay());
        DateTime quitTime = getTodayStartOrEndTime(quitDay.toDate(), bookingRule.getQuitTime());
        scheduleOrderVo.setQuitTime(quitTime.toDate());

        scheduleOrderVo.setReserveDate(schedule.getWorkDate());
        scheduleOrderVo.setReserveTime(schedule.getWorkTime());

        //获取开始预约时间
        DateTime startTime = getTodayStartOrEndTime(new Date(), bookingRule.getReleaseTime());
        scheduleOrderVo.setStartTime(startTime.toDate());
        //获取预约结束时间
        DateTime endTime = getTodayStartOrEndTime(new DateTime().plusDays(bookingRule.getCycle()).toDate(), bookingRule.getStopTime());
        scheduleOrderVo.setEndTime(endTime.toDate());
        //获取当天截止时间
        DateTime stopTime = getTodayStartOrEndTime(schedule.getWorkDate(), bookingRule.getStopTime());
        scheduleOrderVo.setStopTime(stopTime.toDate());
        scheduleOrderVo.setTitle(schedule.getTitle());
        return scheduleOrderVo;
    }

    @Override
    public void saveSchedule(Map<String, Object> map) {
        Schedule schedule = JSONObject.parseObject(JSONObject.toJSONString(map),Schedule.class);
        Query query = new Query(Criteria.where("hoscode").is(schedule.getHoscode()).and("depcode").is(schedule.getDepcode()).and("hosScheduleId").is(map.get("hosScheduleId")));
        Schedule platFormSchedule = mongoTemplate.findOne(query, Schedule.class);
        if(platFormSchedule == null){
            schedule.setCreateTime(new Date());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(0);
            mongoTemplate.save(schedule);
        }else {
            schedule.setCreateTime(new Date());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(0);
            schedule.setId(platFormSchedule.getId());
            mongoTemplate.save(schedule);
        }
    }

    @Override
    public Map<String, Object> findSchedulePage(Map<String, String> map) {
        int pageNum = Integer.parseInt(map.get("pageNum"));
        int pageSize = Integer.parseInt(map.get("pageSize"));
        Query query = new Query(Criteria.where("hoscode").is(map.get("hoscode")).and("isDeleted").is(0));
        long totalElements = mongoTemplate.count(query, Schedule.class);
        query.skip((pageNum-1)*pageSize);
        query.limit(pageSize);
        List<Schedule> content = mongoTemplate.find(query, Schedule.class);


        Map<String, Object> result = new HashMap<>();
        result.put("content",content);
        result.put("totalElements",totalElements);
        return result;
    }

    @Override
    public void removeSchedule(Map<String, String> map) {
        Query query = new Query(Criteria.where("hoscode").is(map.get("hoscode"))
                .and("hosScheduleId").is(map.get("hosScheduleId")));
        List<Schedule> schedules = mongoTemplate.find(query, Schedule.class);
        Update update = new Update();
        update.set("isDeleted",1);
        mongoTemplate.updateFirst(query,update,Schedule.class);
    }

    @Override
    public Map<String, Object> page(Integer pageNum, Integer pageSize, String hoscode, String depcode) {
        //设置查询条件
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),    //查询条件
                Aggregation.group("workDate")  //分组字段
                        .first("workDate").as("workDate")  //设置分组后的第一个字段并起一个别名
                        .sum("reservedNumber").as("reservedNumber")  //可以预约的总人数
                        .sum("availableNumber").as("availableNumber"),  //剩余预约的人数
                //进行排序
                Aggregation.sort(Sort.Direction.ASC, "workDate"),
                //进行分页
                Aggregation.skip((pageNum - 1) * pageSize),
                Aggregation.limit(pageSize));
        //调用方法执行查询
        AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> mappedResults = aggregate.getMappedResults();

        //重复一次上面步骤不添加排序和分页以获取总记录数
        Aggregation aggregation2 = Aggregation.newAggregation(
                Aggregation.match(criteria),    //查询条件
                Aggregation.group("workDate"));
        AggregationResults<BookingScheduleRuleVo> aggregate2 = mongoTemplate.aggregate(aggregation2, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> mappedResults2 = aggregate2.getMappedResults();
        Integer total = mappedResults2.size();

        //对集合进行遍历，给集合中的对象dayOfWeed进行赋值
        for (BookingScheduleRuleVo mappedResult : mappedResults) {
            Date workDate = mappedResult.getWorkDate();
            //调用joda包中的方法转换日期数据类型
            DateTime dateTime = new DateTime(workDate);
            //调用私有的转换工具
            String dayOfWeek = this.parseDateTime(dateTime);
            mappedResult.setDayOfWeek(dayOfWeek);
        }

        //创建一个map集合封装数据
        HashMap<String, Object> map = new HashMap<>();
        map.put("total",total);
        map.put("bookingScheduleList",mappedResults);
        return map;
    }

    private String parseDateTime(DateTime dateTime){
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()){
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "周六";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "周一";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "周二";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "周三";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "周四";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "周五";
                break;
            default:
                dayOfWeek = "周日";
                break;
        }
        return dayOfWeek;
    }

    @Override
    public List<Schedule> getDetailSchedule(String hoscode, String depcode, String workDate) {
        List<Schedule> scheduleList = scheduleRepository.findScheduleByHoscodeAndDepcodeAndWorkDate(hoscode, depcode, new DateTime(workDate).toDate());
        scheduleList.forEach(item -> {
            this.packageSchedule(item);
        });
        //log.info("scheduleList ============> " + scheduleList);
        for (Schedule schedule : scheduleList) {
            String s = parseDateTime(new DateTime(schedule.getWorkDate()));
            HashMap<String, Object> map = new HashMap<>();
            map.put("dayOfWeek",s);
            schedule.setParam(map);
        }
        return scheduleList;
    }

    private Schedule packageSchedule(Schedule schedule){
        schedule.getParam().put("hosname",hospitalService.getHosname(schedule.getHoscode()));
        schedule.getParam().put("depname",departmentService.getDepname(schedule.getDepcode()));
        schedule.getParam().put("dayOfWeek",this.parseDateTime(new DateTime(schedule.getWorkDate())));
        return schedule;
    }

    //获取排班规则
    @Override
    public Map<String, Object> getBookingScheduleRule(Integer page, Integer limit, String hoscode, String depcode) {
        //通过医院id查询医院信息
        Hospital hospital = hospitalService.show(hoscode);
        if(null == hospital) {
            throw new YyghException();
        }
        //获取该医院的预约规则
        BookingRule bookingRule = hospital.getBookingRule();
        //获取在预约周期内的page信息
        IPage<Date> dateIPage = getListData(page,limit,bookingRule);
        //获取可以预约的日期集合
        List<Date> dateRecords = dateIPage.getRecords();

        log.info(dateRecords.toString());

        //通过dateWork进行分组查询
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode).and("workDate").in(dateRecords);
        Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(criteria),
                Aggregation.group("workDate").first("workDate").as("workDate")
                        .count().as("docCount")
                        .sum("availableNumber").as("availableNumber")
                        .sum("reservedNumber").as("reservedNumber")
        );
        AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> scheduleList = aggregate.getMappedResults();
        //将日期和预约信息对象进行合并
        Map<Date, BookingScheduleRuleVo> scheduleVoMap = new HashMap<>();
        if(!CollectionUtils.isEmpty(scheduleList)){
            scheduleVoMap = scheduleList.stream().collect(Collectors.toMap(BookingScheduleRuleVo :: getWorkDate, BookingScheduleRuleVo -> BookingScheduleRuleVo));
        }

        //创建一个vo对象的集合存储当前页面的预约信息
        ArrayList<BookingScheduleRuleVo> bookingScheduleRuleVoList = new ArrayList<>();
        //遍历查询到的日期集合
        for (int i = 0; i < dateRecords.size(); i++) {
            Date date = dateRecords.get(i);
            BookingScheduleRuleVo bookingScheduleRuleVo = scheduleVoMap.get(date); //获取这一天的预约信息
            //判断是否为空
            if(null == bookingScheduleRuleVo){
                //表示当天没有可以预约医生
                bookingScheduleRuleVo = new BookingScheduleRuleVo();
                //设置当天的医生数
                bookingScheduleRuleVo.setDocCount(0);
                //设置剩余可以预约号的数目 -1表示无号
                bookingScheduleRuleVo.setAvailableNumber(-1);
            }
            //设置当天的日期
            bookingScheduleRuleVo.setWorkDate(date);
            bookingScheduleRuleVo.setWorkDateMd(date);

            //设置最后一页的最后一条数据为即将可以预约的号 0正常 1即将放号 -1当天已停止放号
            if(page == dateIPage.getPages() && i == dateRecords.size() - 1){
                bookingScheduleRuleVo.setStatus(1);
            }else {
                bookingScheduleRuleVo.setStatus(0);
            }

            //如果当天预约超过了放号截止时间，不能预约
            if(page == 1 && i == 0){
                //获取截止时间
                DateTime stopTime = getTodayStartOrEndTime(new Date(),bookingRule.getStopTime());
                if(stopTime.isBefore(new DateTime())){
                    bookingScheduleRuleVo.setStatus(-1);
                }
            }
            bookingScheduleRuleVoList.add(bookingScheduleRuleVo);
        }
        //可预约日期规则数据
        HashMap<String, Object> result = new HashMap<>();
        result.put("bookingScheduleList", bookingScheduleRuleVoList);
        result.put("total", dateIPage.getTotal());
        //其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        //医院名称
        baseMap.put("hosname", hospitalService.show(hoscode).getHosname());
        //科室
        Department department =departmentService.getDepartment(hoscode, depcode);
        //大科室名称
        baseMap.put("bigname", department.getBigname());
        //科室名称
        baseMap.put("depname", department.getDepname());
        //月
        baseMap.put("workDateString", new DateTime().toString("yyyy年MM月"));
        //放号时间
        baseMap.put("releaseTime", bookingRule.getReleaseTime());
        //停号时间
        baseMap.put("stopTime", bookingRule.getStopTime());
        result.put("baseMap", baseMap);
        return result;
    }

    private IPage getListData(Integer page, Integer limit, BookingRule bookingRule) {
        //获取预约周期
        Integer cycle = bookingRule.getCycle();
        //获取每天的预约开始时间
        DateTime releaseTime = getTodayStartOrEndTime(new Date(),bookingRule.getReleaseTime());
        //判断当前时间是否达到当天的预约时间
        if(releaseTime.isBeforeNow()){
            cycle += 1;
        };
        //创建一个日期类型的集合
        ArrayList<Date> pageList = new ArrayList<>();
        //获取可以预约的所有日期
        for (Integer i = 0; i < cycle; i++) {
            DateTime dateTime = new DateTime().plusDays(i);
            String s = dateTime.toString("yyyy-MM-dd");
            pageList.add(new DateTime(s).toDate());
        }

        //获取当前页的日期数据
        ArrayList<Date> currentPageDate = new ArrayList<>();
        //获取当前页开始数据和结束数据
        Integer start = (page-1) * limit;
        Integer end = (page-1) * limit + limit;
        //判断end是否超过最后一条数据
        end = end > pageList.size() ? pageList.size() : end;
        for (int i = start; i < end ; i++) {
            currentPageDate.add(pageList.get(i));
        }


        //创建Ipage对象
        IPage<Date> dateIPage = new Page<>(page,7,pageList.size());
        dateIPage.setRecords(currentPageDate);
        return dateIPage;
    }

    private DateTime getTodayStartOrEndTime(Date date, String releaseTime) {
        String dateTimeStr = new DateTime(date).toString("yyyy-MM-dd") + " " + releaseTime;
        DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(dateTimeStr);
        return dateTime;
    }
}
