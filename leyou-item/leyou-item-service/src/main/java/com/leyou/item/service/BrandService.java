package com.leyou.item.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.leyou.item.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.pojo.Brand;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @author Qin PengCheng
 * @date 2018/5/28
 */
@Service
public class BrandService {

    @Autowired
    private BrandMapper brandMapper;

    public PageResult<Brand> queryBrandByPage(

            String key, int page, int rows, String sortBy, Boolean desc) {
        //开始分页
        PageHelper.startPage(page, rows);
        //开始过滤
        Example example = new Example(Brand.class);
        if (!StringUtils.isBlank(key)){
            example.createCriteria().orLike("name","%"+key+"%").orLike("letter",key);
        }
        if (!StringUtils.isBlank(sortBy)){
            String sort = sortBy+(desc ?" asc":" desc");
            example.setOrderByClause(sort);
        }
        //分页查询
        Page<Brand> pageinfo = (Page<Brand>) brandMapper.selectByExample(example);
        return new PageResult<Brand>(pageinfo.getTotal(),pageinfo);
    }
}
