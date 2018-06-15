package com.leyou.item.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.leyou.common.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.cart.pojo.Brand;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    /**
     * 品牌的分页查询
     *
     * @param key
     * @param page
     * @param rows
     * @param sortBy
     * @param desc
     * @return
     */
    public PageResult<Brand> queryBrandByPage(

            String key, int page, int rows, String sortBy, Boolean desc) {
        //开始分页
        PageHelper.startPage(page, rows);
        //开始过滤
        Example example = new Example(Brand.class);
        if (!StringUtils.isBlank(key)) {
            example.createCriteria().orLike("name", "%" + key + "%").orLike("letter", key);
        }
        if (!StringUtils.isBlank(sortBy)) {
            String sort = sortBy + (desc ? " asc" : " desc");
            example.setOrderByClause(sort);
        }
        //分页查询
        Page<Brand> pageinfo = (Page<Brand>) brandMapper.selectByExample(example);
        return new PageResult<Brand>(pageinfo.getTotal(), pageinfo);
    }


    /**
     * 品牌的新增
     *
     * @param categories
     * @param brand
     */
    @Transactional
    public void saveBrand(List<Long> categories, Brand brand) {
        //先增加品牌
        try {
            this.brandMapper.insertSelective(brand);
            //再维护中间表
            for (Long c : categories) {
                brandMapper.insertCategoryBrand(c, brand.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 品牌的修改
     *
     * @param categories
     * @param brand
     */
    @Transactional
    public void updateBrand(List<Long> categories, Brand brand) {
        //修改品牌
        brandMapper.updateByPrimaryKeySelective(brand);
        //维护中间表
        for (Long categoryId : categories) {
            brandMapper.updateCategoryBrand(categoryId, brand.getId());
        }

    }

    /**
     * 品牌的删除后
     * @param bid
     */
    public void deleteBrand(Long bid) {
        //删除品牌表
        brandMapper.deleteByPrimaryKey(bid);
        //维护中间表
        brandMapper.deleteCategoryBrandByBid(bid);
    }

    public String queryBrandNameByBid(Long brandId) {
        Brand brand = this.brandMapper.selectByPrimaryKey(brandId);
        return brand.getName();
    }

    /**
     * 根据cid查到所有的品牌
     * @param cid
     * @return
     */
    public List<Brand> queryBrandsByCategoryId(Long cid) {
        //先根据cid查到所有的品牌id
        List<Long> bids = brandMapper.selectBrandIdsByCategoryId(cid);
        //根据品牌id查到所有的信息
        return this.queryBrandsByBids(bids);
    }


    /**
     * 根据bid的集合查询商品信息
     */
    public List<Brand> queryBrandsByBids(List<Long> bids){
        return this.brandMapper.selectByIdList(bids);
    }
}
