package com.leyou.item.controller;

import com.leyou.cart.pojo.Category;
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

    @GetMapping("list")
    public ResponseEntity<List<Category>> queryCategoryByPid(@RequestParam(value = "pid", defaultValue = "0") Long pid) {

        List<Category> list = categoryService.queryCategoryByPid(pid);
        //判断集合是否为空，或者查到数据长度为0
        if (list == null || list.size() < 1) {
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
     * 添加商品分类的方法
     *
     * @param category
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> addCategory(Category category) {
        category.setId(null);
        int result = categoryService.addCateGory(category);
        if (result != 1) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    /**
     * 根据id删除分类
     * @param id
     * @return
     */
    @DeleteMapping("delete")
    public ResponseEntity<Void> deleteCategory(
            @RequestParam("id") Long id
    ) {
        int result = categoryService.deleteCateGory(id);
        if (result != 1) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    /**
     * 修改商品分类的方法
     *
     * @param id
     * @param name
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> updateCategory(@RequestParam("id") Long id, @RequestParam("name") String name) {
        int result = categoryService.updateCategory(id, name);
        if (result != 1) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    /**
     * 根据分类id的集合查询分类名称的集合
     * @param list
     * @return
     */
    @GetMapping("names")
    public ResponseEntity<List<String>> queryCategoryNamesBycids(
            @RequestParam("ids") List<Long> list
    ){
        List names = this.categoryService.queryCategoryNameByCids(list);
        if (names==null||names.size()<1){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.status(HttpStatus.OK).body(names);
    }

    /**
     * 根据分类的cid查询所有的分类对象
     * @param cids
     * @return
     */
    @GetMapping("categories")
    public ResponseEntity<List<Category>> queryCategoriesByCids(
            @RequestParam("cids") List<Long> cids
    ){
      List<Category> categories  =  this.categoryService.queryCategoriesByCids(cids);
      if (categories==null||categories.size()<1){
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
      }
        return ResponseEntity.status(HttpStatus.OK).body(categories);
    }

    /**
     * 查询三级分类
     * @param id
     * @return
     */
    @GetMapping("level/{id}")
    public ResponseEntity<List<Category>> queryParentByCid3(
            @PathVariable("id") Long id){
        List<Category> categories  = this.categoryService.queryParentByCid3(id);
        if (categories==null&&categories.size()<1){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.status(HttpStatus.OK).body(categories);
    }
}
