package com.leyou.item.controller;

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


    @GetMapping("list")
    public ResponseEntity<List<Category>> queryCategoryByPid(@RequestParam(value = "pid" ,defaultValue = "0") Long pid){
        List<Category> list = categoryService.queryCategoryByPid(pid);
        //判断集合是否为空，或者查到数据长度为0
        if (list==null||list.size()<1){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    /**
     * 添加商品分类的方法
     * @param name
     * @param pid
     * @param isParent
     * @param sort
     * @return
     */
    @PostMapping("add")
    public ResponseEntity<Void> addCategory(Category category){
       int result =  categoryService.addCateGory(category);
       if (result!=1){
           return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
       }
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    @DeleteMapping("delete")
    public ResponseEntity<Void> deleteCategory(
            @RequestParam("id")Long id
    ){
        int result =  categoryService.deleteCateGory(id);
        if (result!=1){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
}
