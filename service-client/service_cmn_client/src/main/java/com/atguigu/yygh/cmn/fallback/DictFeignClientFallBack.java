package com.atguigu.yygh.cmn.fallback;

import com.atguigu.yygh.cmn.client.DictFeignClient;
import com.atguigu.yygh.model.cmn.Dict;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DictFeignClientFallBack implements FallbackFactory<DictFeignClient> {
    @Override
    public DictFeignClient create(Throwable throwable) {
        return new DictFeignClient() {
            @Override
            public String findNameByParentDictCodeAndValue(Long value) {
                return "服务升级中,稍后重试1";
            }

            @Override
            public String findNameByParentDictCodeAndValue(String parentDictCode, Long value) {
                return "服务升级中,稍后重试2";
            }

            @Override
            public List<Dict> findByDictCode(String dictCode) {
                return null;
            }

            @Override
            public List<Dict> findDictList(Long parentId) {
                return null;
            }
        };
    }
}
