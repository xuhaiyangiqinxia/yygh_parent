package com.atguigu.yygh.cmn.controller;


import com.atguigu.yygh.cmn.service.DictService;
import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.model.cmn.Dict;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

/**
 * <p>
 * 组织架构表 前端控制器
 * </p>
 *
 * @author XuSir
 * @since 2022-11-04
 */
@RestController
@Slf4j
@RequestMapping("/admin/cmn")
@Api(tags = "数据字典管理控制层")
public class DictController {

    @Autowired
    private DictService dictService;


    @GetMapping("/getName/{value}")
    public String findNameByParentDictCodeAndValue( @PathVariable(value = "value") Long value){
        return dictService.findNameByParentDictCodeAndValue("",value);
    }

    @GetMapping("/getName/{parentDictCode}/{value}")
    public String findNameByParentDictCodeAndValue(
            @PathVariable(value = "parentDictCode") String parentDictCode ,
            @PathVariable(value = "value") Long value){
        return dictService.findNameByParentDictCodeAndValue(parentDictCode,value);
    }

    @GetMapping("/findByDictCode/{dictCode}")
    public List<Dict> findByDictCode(@PathVariable(value = "dictCode") String dictCode){
        return dictService.findByDictCode(dictCode);
    }

    @GetMapping("/findByDictCodes/{dictCode}")
    public R findByDictCodes(@PathVariable(value = "dictCode") String dictCode){
        return R.ok().data("list",dictService.findByDictCode(dictCode));
    }

    @GetMapping("/findDictList/{parentId}")
    public  List<Dict> findDictList(@PathVariable("parentId") Long parentId){
        return dictService.getDictData(parentId);
    }

    @GetMapping("/dict/{pid}")
    public R findDictData(@PathVariable Long pid){
        List<Dict> dictList = dictService.getDictData(pid);
        return R.ok().data("dictList",dictList);
    }

    @GetMapping("/download")
    public void download(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("测试", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        dictService.download(response);
    }

    @PostMapping("upload")
    public R upload(MultipartFile file) throws IOException {
        //System.out.println("进来了");
        dictService.upload(file);
        return R.ok();
    }
}

