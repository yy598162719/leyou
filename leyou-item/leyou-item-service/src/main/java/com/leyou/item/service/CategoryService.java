package com.leyou.item.service;

import com.leyou.item.mapper.CategoryMapper;
import com.leyou.cart.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
     *
     * @param pid
     * @return
     */
    public List<Category> queryCategoryByPid(Long pid) {
        Category category = new Category();
        category.setParentId(pid);
        return categoryMapper.select(category);

    }

    /**
     * 根据品牌信息查询商品分类
     * @param bid
     * @return
     */
    public List<Category> queryCategoryByBid(Long bid) {
        return this.categoryMapper.queryCategoryByBid(bid);
    }

    /**
     * 添加商品分类
     *
     * @param category
     * @return
     */
    @Transactional
    public int addCateGory(Category category) {
        categoryMapper.insert(category);
        //将商品分类的父亲的isparent设为父。
        Long id = category.getParentId();
        //将此id的isparent设置为true
        Category parent = new Category();
        parent.setId(id);
        parent.setIsParent(true);
        int result = categoryMapper.updateByPrimaryKeySelective(parent);
        return result ;
    }


    /**
     * 修改商品分类
     *
     * @param id
     * @param name
     * @return
     */
    @Transactional
    public int updateCategory(Long id, String name) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        int result = categoryMapper.updateByPrimaryKeySelective(category);
        return result;
    }

    /**
     * 删除商品分类
     *
     * @param id
     * @return
     */
    @Transactional
    public int deleteCateGory(Long id) {

        Category category = categoryMapper.selectByPrimaryKey(id);
        //递归删除儿子以及后辈
        this.deleteChild(category);
        //删除完成之后，需要判断父亲还有没有子节点，如果没有，父亲的isparent置为0
        //找出所有的兄弟元素
        Category c2 = new Category();
        c2.setParentId(category.getParentId());
        List<Category> list = categoryMapper.select(c2);
        //当没有兄弟节点的时候
        if (list == null || list.size()<1) {
            //判断自己是不是根节点
            if (category.getParentId() != 0) {
                //将父节点的isparent置为0
                Category parent = new Category();
                parent.setId(category.getParentId());
                parent.setIsParent(false);
                categoryMapper.updateByPrimaryKeySelective(parent);
            }
        }
        return 1;
    }

    /**
     * 递归删除子节点
     *
     * @param category
     */
    @Transactional
    public void deleteChild(Category category) {
        //存在子节点，删除自己以及后辈
        if (category.getIsParent()) {
            List<Category> list = this.queryCategoryByPid(category.getId());
            for (Category c : list) {
                //删除这个节点，递归删除之后的节点
                //删除自己
                this.categoryMapper.deleteByPrimaryKey(category);
                //删除后辈
                this.deleteChild(c);
            }
        } else {
            //如果不是父亲，直接删除
                this.categoryMapper.deleteByPrimaryKey(category);
        }

    }

    /**
     * 根据多个cids，查询得到一个cname的集合
     * @param cids
     * @return
     */
    public List queryCategoryNameByCids(List cids) {
        List<Category> list = this.categoryMapper.selectByIdList(cids);
        ArrayList<String> names = new ArrayList<>();
        for (Category category : list) {
            names.add(category.getName());
        }
        return names;
    }

    public List<Category> queryCategoriesByCids(List<Long> cids) {
        List<Category> categories = this.categoryMapper.selectByIdList(cids);
        return categories;
    }

    public List<Category> queryParentByCid3(Long id) {
        //创建一个list集合来接收
        List<Category> categories = new ArrayList<>();
        Category category3 = this.categoryMapper.selectByPrimaryKey(id);
        Category category2 = this.categoryMapper.selectByPrimaryKey(category3.getParentId());
        Category category1 = this.categoryMapper.selectByPrimaryKey(category2.getParentId());
        categories.add(category1);
        categories.add(category2);
        categories.add(category3);
        return categories;
    }
}
