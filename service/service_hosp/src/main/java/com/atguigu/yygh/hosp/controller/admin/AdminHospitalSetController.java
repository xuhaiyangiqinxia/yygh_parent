package com.atguigu.yygh.hosp.controller.admin;


import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.common.utils.MD5;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.model.hosp.HospitalSet;
import com.atguigu.yygh.vo.hosp.HospitalSetQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

/**
 * <p>
 * 医院设置表 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2022-10-29
 */
@RestController
@RequestMapping("admin/hospset")
@Api(tags = "医院设置控制层")
@Slf4j
public class AdminHospitalSetController {


    @Autowired
    private HospitalSetService hospitalSetService;

    @ApiOperation(value = "查询所有医院设置信息")
    @GetMapping("/all")
    public R findAll(){
        try {
            Integer sum = 10/1;
        } catch (Exception e) {
            throw new YyghException(30001,"自定义的异常");
        }
        List<HospitalSet> hospitalSets = hospitalSetService.list();
        return R.ok().data("list",hospitalSets);
    }

    @ApiOperation("根据医院id获取医院设置信息")
    @GetMapping("/{id}")
    public R getHospSetById(
            @ApiParam(name = "id",value = "医院的id",required = true)
            @PathVariable Long id
    ){
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        return R.ok().data("hospitalSet",hospitalSet);
    }


    @ApiOperation(value = "医院设置分页列表")
    @PostMapping("/{page}/{limit}")
    public R page(
            @ApiParam(name = "page",value = "当前页码",required = true)
            @PathVariable Integer page,
            @ApiParam(name = "limit",value = "每页记录数",required = true)
            @PathVariable Integer limit,
            @RequestBody HospitalSetQueryVo vo
            ){
        QueryWrapper<HospitalSet> wrapper = new QueryWrapper<>();
        System.out.println("进来了");
        System.out.println(vo);
        if (!StringUtils.isEmpty(vo.getHosname())){
            wrapper.like("hosname",vo.getHosname());
        }
        if(!StringUtils.isEmpty(vo.getHoscode())) {
            wrapper.eq("hoscode", vo.getHoscode());
        }
        Page<HospitalSet> pageParam = new Page<>(page, limit);
        hospitalSetService.page(pageParam,wrapper);
        return R.ok().data("total",pageParam.getTotal()).data("rows",pageParam.getRecords());
    }

    @ApiOperation("新增医院设置")
    @PostMapping("/saveHospSet")
    public R save(
        @ApiParam(name = "hospital",value = "医院设置的对象",required = true)
        @RequestBody HospitalSet hospitalSet
    ){
        //设置医院的状态
        hospitalSet.setStatus(0);
        //为医院生成密钥
        Random random = new Random();
        String signKey = MD5.encrypt(System.currentTimeMillis() + "" + random.nextInt(1000));
        hospitalSet.setSignKey(signKey);
        boolean result = hospitalSetService.save(hospitalSet);
        if(result){
            System.out.println("ok");
            return R.ok();
        }
        System.out.println("err");
        return R.error().message("保存医院设置失败");
    }


    @ApiOperation("修改医院设置")
    @PutMapping("/updateHospSet")
    public R updateHospSetById(
            @ApiParam(name = "hospitalSet",value = "医院设置的对象",required = true)
            @RequestBody HospitalSet hospitalSet
    ){
        hospitalSetService.updateById(hospitalSet);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return R.ok();
    }

    @PutMapping("/status/{id}/{status}")
    @ApiOperation("修改医院的状态")
    public R updateHospStatus(
            @ApiParam(name = "id",value = "医院的id",required = true)
            @PathVariable Long id,
            @ApiParam(name = "status",value = "医院的状态",required = true)
            @PathVariable Integer status
    ){
        HospitalSet hospitalSet = new HospitalSet();
        hospitalSet.setId(id);
        hospitalSet.setStatus(status);

        hospitalSetService.updateById(hospitalSet);
        return R.ok();
    }

    @ApiOperation(value = "根据医院id删除医院设置信息")
    @DeleteMapping("delete/{id}")
    public R delete(@PathVariable Long id){
        hospitalSetService.removeById(id);
        return R.ok();
    }


    @ApiOperation("批量删除医院设置信息")
    @DeleteMapping("/batchRemove")
    public R batchRemoveHospSet(
            @ApiParam(name = "ids",value = "要删除的所有医院的id",required = true)
            @RequestBody List<Long> ids
    ){
        hospitalSetService.removeByIds(ids);
        return R.ok();
    }



















   /* @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "id", defaultValue = "1", type = "Long", dataType = "query",value = "医院id"),
            @ApiImplicitParam(name = "name", type = "String", dataType = "path",value = "医院名字")
    })
    @GetMapping("/getHospSet/{name}")
    @ApiOperation(value = "根据医院id获取医院设置信息")
   // @ApiResponse(code = 200,message = "成功",response = HospitalSet.class)
    public HospitalSet getHospitalSetById(
            //@ApiParam(name = "id",value = "医院的id",readOnly = false,required = true,defaultValue = "1")
            @RequestParam Long id,
            @PathVariable("name") String name
    ){
        return new HospitalSet();
    }*/

}

