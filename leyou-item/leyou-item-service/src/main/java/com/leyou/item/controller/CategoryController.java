package com.leyou.item.controller;

import com.leyou.pojo.Brand;
import com.leyou.pojo.Category;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Qin PengCheng
 * @date 2018/5/27
 */
@RestController
@RequestMapping("category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 根据分类的父id查询商品的分类
     * @param pid
     * @return
     */
    @RequestMapping("list")
    public ResponseEntity<List<Category>> queryCategoryByPid(@RequestParam(value = "pid" ,defaultValue = "0") Long pid){
        List<Category> list = categoryService.queryCategoryByPid(pid);
        //判断集合是否为空，或者查到数据长度为0
        if (list==null||list.size()<1){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    /**
     * 根据bid（品牌id）查询商品的信息
     * @param bid
     * @return
     */
    @GetMapping("bid/{bid}")
    public ResponseEntity<List<Category>> queryCategoryByBid(@PathVariable("bid")Long bid){
        List<Category> list = this.categoryService.queryCategoryByBid(bid);
        if (list==null||list.size()<1){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    /**
     *
     */
}
