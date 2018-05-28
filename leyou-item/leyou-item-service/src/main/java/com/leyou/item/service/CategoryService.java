package com.leyou.item.service;

import com.leyou.item.mapper.CategoryMapper;
import com.leyou.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Qin PengCheng
 * @date 2018/5/27
 */
@Service
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 查询商品分类的方法
     * @param pid
     * @return
     */
    public List<Category> queryCategoryByPid(Long pid) {
        Category category = new Category();
        category.setParentId(pid);
        return categoryMapper.select(category);
    }

    /**
     * 添加商品分类
     * @param category
     * @return
     */
    public int addCateGory(Category category) {
        category.setSort(null);
        int result = categoryMapper.insert(category);
        return result;
    }

    public int deleteCateGory(Long id) {
        Category category = new Category();
        category.setId(id);
        int result = categoryMapper.delete(category);
        return result;
    }
}
