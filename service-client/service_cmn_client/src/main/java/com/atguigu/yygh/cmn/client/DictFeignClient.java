package com.atguigu.yygh.cmn.client;

import com.atguigu.yygh.cmn.fallback.DictFeignClientFallBack;
import com.atguigu.yygh.model.cmn.Dict;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(value = "service-cmn",path = "/admin/cmn",fallbackFactory = DictFeignClientFallBack.class)

public interface DictFeignClient {


    @GetMapping("/getName/{value}")
    public String findNameByParentDictCodeAndValue( @PathVariable(value = "value") Long value);


    @GetMapping("/getName/{parentDictCode}/{value}")
    public String findNameByParentDictCodeAndValue(
            @PathVariable(value = "parentDictCode") String parentDictCode ,
            @PathVariable(value = "value") Long value);

    @GetMapping("/findByDictCode/{dictCode}")
    public List<Dict> findByDictCode(@PathVariable(value = "dictCode") String dictCode);

    @GetMapping("/findDictList/{parentId}")
    public  List<Dict> findDictList(@PathVariable("parentId") Long parentId);
}
