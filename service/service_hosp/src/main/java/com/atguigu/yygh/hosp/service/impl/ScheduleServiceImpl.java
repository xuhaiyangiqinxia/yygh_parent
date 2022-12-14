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
            throw new YyghException(2006,"??????id??????");
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
        //??????????????????
        if(schedule == null){
            throw new YyghException(2003,"????????????????????????");
        }
        String hoscode = schedule.getHoscode();
        Hospital hospital = hospitalService.show(hoscode);
        if(hospital ==null){
            throw new YyghException(2003,"????????????????????????");
        }
        BookingRule bookingRule = hospital.getBookingRule();
        //???ScheduleOrderVo??????
        ScheduleOrderVo scheduleOrderVo = new ScheduleOrderVo();
        scheduleOrderVo.setDepcode(schedule.getDepcode());
        scheduleOrderVo.setHoscode(schedule.getHoscode());
        scheduleOrderVo.setHosname(departmentService.getDepname(schedule.getDepcode()));
        scheduleOrderVo.setAmount(schedule.getAmount());
        scheduleOrderVo.setAvailableNumber(schedule.getAvailableNumber());
        scheduleOrderVo.setHosScheduleId(schedule.getHosScheduleId());
        //????????????????????????
        DateTime quitDay = new DateTime(schedule.getWorkDate()).plusDays(bookingRule.getQuitDay());
        DateTime quitTime = getTodayStartOrEndTime(quitDay.toDate(), bookingRule.getQuitTime());
        scheduleOrderVo.setQuitTime(quitTime.toDate());

        scheduleOrderVo.setReserveDate(schedule.getWorkDate());
        scheduleOrderVo.setReserveTime(schedule.getWorkTime());

        //????????????????????????
        DateTime startTime = getTodayStartOrEndTime(new Date(), bookingRule.getReleaseTime());
        scheduleOrderVo.setStartTime(startTime.toDate());
        //????????????????????????
        DateTime endTime = getTodayStartOrEndTime(new DateTime().plusDays(bookingRule.getCycle()).toDate(), bookingRule.getStopTime());
        scheduleOrderVo.setEndTime(endTime.toDate());
        //????????????????????????
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
        //??????????????????
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),    //????????????
                Aggregation.group("workDate")  //????????????
                        .first("workDate").as("workDate")  //???????????????????????????????????????????????????
                        .sum("reservedNumber").as("reservedNumber")  //????????????????????????
                        .sum("availableNumber").as("availableNumber"),  //?????????????????????
                //????????????
                Aggregation.sort(Sort.Direction.ASC, "workDate"),
                //????????????
                Aggregation.skip((pageNum - 1) * pageSize),
                Aggregation.limit(pageSize));
        //????????????????????????
        AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> mappedResults = aggregate.getMappedResults();

        //?????????????????????????????????????????????????????????????????????
        Aggregation aggregation2 = Aggregation.newAggregation(
                Aggregation.match(criteria),    //????????????
                Aggregation.group("workDate"));
        AggregationResults<BookingScheduleRuleVo> aggregate2 = mongoTemplate.aggregate(aggregation2, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> mappedResults2 = aggregate2.getMappedResults();
        Integer total = mappedResults2.size();

        //?????????????????????????????????????????????dayOfWeed????????????
        for (BookingScheduleRuleVo mappedResult : mappedResults) {
            Date workDate = mappedResult.getWorkDate();
            //??????joda???????????????????????????????????????
            DateTime dateTime = new DateTime(workDate);
            //???????????????????????????
            String dayOfWeek = this.parseDateTime(dateTime);
            mappedResult.setDayOfWeek(dayOfWeek);
        }

        //????????????map??????????????????
        HashMap<String, Object> map = new HashMap<>();
        map.put("total",total);
        map.put("bookingScheduleList",mappedResults);
        return map;
    }

    private String parseDateTime(DateTime dateTime){
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()){
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "??????";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "??????";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "??????";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "??????";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "??????";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "??????";
                break;
            default:
                dayOfWeek = "??????";
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

    //??????????????????
    @Override
    public Map<String, Object> getBookingScheduleRule(Integer page, Integer limit, String hoscode, String depcode) {
        //????????????id??????????????????
        Hospital hospital = hospitalService.show(hoscode);
        if(null == hospital) {
            throw new YyghException();
        }
        //??????????????????????????????
        BookingRule bookingRule = hospital.getBookingRule();
        //???????????????????????????page??????
        IPage<Date> dateIPage = getListData(page,limit,bookingRule);
        //?????????????????????????????????
        List<Date> dateRecords = dateIPage.getRecords();

        log.info(dateRecords.toString());

        //??????dateWork??????????????????
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode).and("workDate").in(dateRecords);
        Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(criteria),
                Aggregation.group("workDate").first("workDate").as("workDate")
                        .count().as("docCount")
                        .sum("availableNumber").as("availableNumber")
                        .sum("reservedNumber").as("reservedNumber")
        );
        AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> scheduleList = aggregate.getMappedResults();
        //??????????????????????????????????????????
        Map<Date, BookingScheduleRuleVo> scheduleVoMap = new HashMap<>();
        if(!CollectionUtils.isEmpty(scheduleList)){
            scheduleVoMap = scheduleList.stream().collect(Collectors.toMap(BookingScheduleRuleVo :: getWorkDate, BookingScheduleRuleVo -> BookingScheduleRuleVo));
        }

        //????????????vo????????????????????????????????????????????????
        ArrayList<BookingScheduleRuleVo> bookingScheduleRuleVoList = new ArrayList<>();
        //??????????????????????????????
        for (int i = 0; i < dateRecords.size(); i++) {
            Date date = dateRecords.get(i);
            BookingScheduleRuleVo bookingScheduleRuleVo = scheduleVoMap.get(date); //??????????????????????????????
            //??????????????????
            if(null == bookingScheduleRuleVo){
                //????????????????????????????????????
                bookingScheduleRuleVo = new BookingScheduleRuleVo();
                //????????????????????????
                bookingScheduleRuleVo.setDocCount(0);
                //???????????????????????????????????? -1????????????
                bookingScheduleRuleVo.setAvailableNumber(-1);
            }
            //?????????????????????
            bookingScheduleRuleVo.setWorkDate(date);
            bookingScheduleRuleVo.setWorkDateMd(date);

            //?????????????????????????????????????????????????????????????????? 0?????? 1???????????? -1?????????????????????
            if(page == dateIPage.getPages() && i == dateRecords.size() - 1){
                bookingScheduleRuleVo.setStatus(1);
            }else {
                bookingScheduleRuleVo.setStatus(0);
            }

            //????????????????????????????????????????????????????????????
            if(page == 1 && i == 0){
                //??????????????????
                DateTime stopTime = getTodayStartOrEndTime(new Date(),bookingRule.getStopTime());
                if(stopTime.isBefore(new DateTime())){
                    bookingScheduleRuleVo.setStatus(-1);
                }
            }
            bookingScheduleRuleVoList.add(bookingScheduleRuleVo);
        }
        //???????????????????????????
        HashMap<String, Object> result = new HashMap<>();
        result.put("bookingScheduleList", bookingScheduleRuleVoList);
        result.put("total", dateIPage.getTotal());
        //??????????????????
        Map<String, String> baseMap = new HashMap<>();
        //????????????
        baseMap.put("hosname", hospitalService.show(hoscode).getHosname());
        //??????
        Department department =departmentService.getDepartment(hoscode, depcode);
        //???????????????
        baseMap.put("bigname", department.getBigname());
        //????????????
        baseMap.put("depname", department.getDepname());
        //???
        baseMap.put("workDateString", new DateTime().toString("yyyy???MM???"));
        //????????????
        baseMap.put("releaseTime", bookingRule.getReleaseTime());
        //????????????
        baseMap.put("stopTime", bookingRule.getStopTime());
        result.put("baseMap", baseMap);
        return result;
    }

    private IPage getListData(Integer page, Integer limit, BookingRule bookingRule) {
        //??????????????????
        Integer cycle = bookingRule.getCycle();
        //?????????????????????????????????
        DateTime releaseTime = getTodayStartOrEndTime(new Date(),bookingRule.getReleaseTime());
        //???????????????????????????????????????????????????
        if(releaseTime.isBeforeNow()){
            cycle += 1;
        };
        //?????????????????????????????????
        ArrayList<Date> pageList = new ArrayList<>();
        //?????????????????????????????????
        for (Integer i = 0; i < cycle; i++) {
            DateTime dateTime = new DateTime().plusDays(i);
            String s = dateTime.toString("yyyy-MM-dd");
            pageList.add(new DateTime(s).toDate());
        }

        //??????????????????????????????
        ArrayList<Date> currentPageDate = new ArrayList<>();
        //??????????????????????????????????????????
        Integer start = (page-1) * limit;
        Integer end = (page-1) * limit + limit;
        //??????end??????????????????????????????
        end = end > pageList.size() ? pageList.size() : end;
        for (int i = start; i < end ; i++) {
            currentPageDate.add(pageList.get(i));
        }


        //??????Ipage??????
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
