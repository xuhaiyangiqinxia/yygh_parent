package com.atguigu.yygh.hosp.controller.admin;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.model.cmn.Dict;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/hosp")
@Slf4j
public class AdminHospitalController {

    @Autowired
    private HospitalService hospitalService;

    @PostMapping("/hospital/{pageNum}/{pageSize}")
    public R findHospPage(@PathVariable("pageNum") Integer pageNum, @PathVariable("pageSize") Integer pageSize,@RequestBody HospitalQueryVo vo){
        Page<Hospital> page = hospitalService.findHospPage(pageNum,pageSize,vo);
        return R.ok().data("current",page.getContent()).data("total",page.getTotalElements());
    }

    @GetMapping("/findByDictCode/{dictCode}")
    public R findByDictCode(@PathVariable("dictCode") String dictCode){
        List<Dict> list =  hospitalService.findByDictCode(dictCode);
        return R.ok().data("list",list);
    }

    @GetMapping("/findDictList/{parentId}")
    public R findDictList(@PathVariable("parentId") Long pid){
        List<Dict> list = hospitalService.findDictList(pid);
        return R.ok().data("list",list);
    }

    @PutMapping("/status/{id}/{status}")
    public R updateStatus(@PathVariable("id") String id,@PathVariable("status") Integer status){
        hospitalService.updateStatus(id,status);
        return R.ok();
    }

    @ApiOperation(value = "医院详情")
    @GetMapping("/show/{id}")
    public R show(@PathVariable("id") String id){
        return R.ok().data("hospitalDetails",hospitalService.getDetails(id));
    }

    @GetMapping("/department/{hoscode}")
    public R findDeptList(@PathVariable("hoscode") String hoscode){
        List<DepartmentVo> result = hospitalService.findDeptList(hoscode);
        return R.ok().data("result",result);
    }


}
