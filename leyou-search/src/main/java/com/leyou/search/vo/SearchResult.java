package com.leyou.search.vo;

import com.leyou.common.PageResult;
import com.leyou.cart.pojo.Brand;
import com.leyou.cart.pojo.Category;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Qin PengCheng
 * @date 2018/6/7
 */
public class SearchResult<Goods> extends PageResult<Goods> {
    public SearchResult(List<Category> categories, List<Brand> brands, List<Map<String, Object>> specs) {
        this.categories = categories;
        this.brands = brands;
        this.specs = specs;
    }

    public SearchResult(Long total, List<Goods> items, List<Category> categories, List<Brand> brands, List<Map<String, Object>> specs) {
        super(total, items);
        this.categories = categories;
        this.brands = brands;
        this.specs = specs;
    }

    public SearchResult(Long total, Long totalPage, List<Goods> items, List<Category> categories, List<Brand> brands, List<Map<String, Object>> specs) {
        super(total, totalPage, items);
        this.categories = categories;
        this.brands = brands;
        this.specs = specs;
    }

    //分类的集合
    private List<Category> categories = new ArrayList<>();

    //品牌的集合
    private List<Brand> brands = new ArrayList<>();

    //规格参数的过滤条件
    private List<Map<String,Object>> specs = new ArrayList<>();

    public List<Map<String, Object>> getSpecs() {
        return specs;
    }

    public void setSpecs(List<Map<String, Object>> specs) {
        this.specs = specs;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public List<Brand> getBrands() {
        return brands;
    }

    public void setBrands(List<Brand> brands) {
        this.brands = brands;
    }
}
