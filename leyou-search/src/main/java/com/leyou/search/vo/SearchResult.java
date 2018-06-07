package com.leyou.search.vo;

import com.leyou.common.PageResult;
import com.leyou.pojo.Brand;
import com.leyou.pojo.Category;
import com.leyou.search.pojo.Goods;
import com.sun.org.glassfish.external.statistics.BoundedRangeStatistic;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Qin PengCheng
 * @date 2018/6/7
 */
public class SearchResult<Goods> extends PageResult<Goods> {

    //分类的集合
    private List<Category> categories = new ArrayList<>();

    //品牌的集合
    private List<Brand> brands = new ArrayList<>();

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
