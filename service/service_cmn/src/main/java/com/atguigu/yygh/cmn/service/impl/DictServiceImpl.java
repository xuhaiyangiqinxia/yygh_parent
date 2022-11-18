package com.atguigu.yygh.cmn.service.impl;

import com.alibaba.excel.EasyExcel;
import com.atguigu.yygh.cmn.listener.DictListener;
import com.atguigu.yygh.cmn.mapper.DictMapper;
import com.atguigu.yygh.cmn.service.DictService;
import com.atguigu.yygh.model.cmn.Dict;
import com.atguigu.yygh.vo.cmn.DictEeVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 组织架构表 服务实现类
 * </p>
 *
 * @author XuSir
 * @since 2022-11-04
 */
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

    @Cacheable("dict")
    @Override
    public List<Dict> getDictData(Long pid) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id",pid);
        List<Dict> dictList = baseMapper.selectList(wrapper);
        for (Dict dict : dictList) {
            if(getChildren(dict.getId())){
                dict.setHasChildren(true);
            }
        }
        return dictList;
    }

    @CacheEvict(value = "dict",allEntries = true)
    @Override
    public void upload(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(),DictEeVo.class,new DictListener(baseMapper)).sheet(0).doRead();
    }

    @Override
    public String findNameByParentDictCodeAndValue(String dictCode, Long value) {
        if (dictCode == ""){
            QueryWrapper<Dict> wrapper = new QueryWrapper<>();
            wrapper.eq("value",value);
            Dict dict = baseMapper.selectOne(wrapper);
            return dict.getName();
        } else {
            Dict dict = findDictByDictCode(dictCode);
            Long pid = dict.getId();
            QueryWrapper<Dict> wrapper2 = new QueryWrapper<>();
            wrapper2.eq("parent_id",pid).eq("value",value);
            Dict dict2 = baseMapper.selectOne(wrapper2);
            return dict2.getName();
        }
    }

    @Override
    public void download(HttpServletResponse response) throws IOException {
        List<Dict> dictList = baseMapper.selectList(null);
        List<DictEeVo> dictEeVoList = new ArrayList<>();
        for (Dict dict : dictList) {
            DictEeVo dictEeVo = new DictEeVo();
            BeanUtils.copyProperties(dict,dictEeVo);
            dictEeVoList.add(dictEeVo);
        }
        EasyExcel.write(response.getOutputStream(), DictEeVo.class).sheet("模板").doWrite(dictEeVoList);
    }

    private Boolean getChildren(Long id){
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id",id);
        Integer count = baseMapper.selectCount(wrapper);
        return count > 0 ? true : false;
    }

    @Override
    public List<Dict> findByDictCode(String dictCode) {
        Dict dict = findDictByDictCode(dictCode);
        return getDictData(dict.getId());
    }

    private Dict findDictByDictCode(String dictCode){
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("dict_code",dictCode);
        return baseMapper.selectOne(wrapper);
    }
}
