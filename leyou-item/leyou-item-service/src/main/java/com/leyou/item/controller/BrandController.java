package com.leyou.item.controller;

import com.leyou.common.PageResult;
import com.leyou.item.service.BrandService;
import com.leyou.cart.pojo.Brand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Qin PengCheng
 * @date 2018/5/28
 */
@RestController
@RequestMapping("brand")
public class BrandController {

    @Autowired
    private BrandService brandService;

    /**
     * 商品品牌的查询
     *
     * @param key
     * @param page
     * @param rows
     * @param sortBy
     * @param desc
     * @return
     */
    @GetMapping("page")
    public ResponseEntity<PageResult> queryBrandByPage(
            @RequestParam("key") String key,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "rows", defaultValue = "5") int rows,
            @RequestParam("sortBy") String sortBy,
            @RequestParam(value = "desc", defaultValue = "false") Boolean desc
    ) {
        PageResult<Brand> pageResult = brandService.queryBrandByPage(key, page, rows, sortBy, desc);
        if (pageResult == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(pageResult);
    }

    /**
     * 品牌的新增
     * @param categories
     * @param brand
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> saveBrand(@RequestParam(value = "cids") List<Long> categories, Brand brand) {
        this.brandService.saveBrand(categories, brand);
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    /**
     *  品牌的修改
     * @param categories
     * @param brand
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> updateBrand(@RequestParam(value = "cids") List<Long> categories, Brand brand) {
        this.brandService.updateBrand(categories, brand);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }


    /**
     * 品牌的删除
     * @param bid
     * @return
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteBrand(@RequestParam(value = "id")Long bid){
        this.brandService.deleteBrand(bid);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    /**
     * 根据分类的集合查出所有的品牌
     * @param cid
     * @return
     */
    @GetMapping("cid/{id}")
    public ResponseEntity<List<Brand>> queryBrandsByCategoryId(@PathVariable(value = "id")Long cid
    ){
      List<Brand> list = this.brandService.queryBrandsByCategoryId(cid);
      if (list==null||list.size()<1){
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
      }
      return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    /**
     * 根据品牌id的集合查询所有的品牌
     * @param brandIds
     * @return
     */
    @GetMapping("bids")
    public ResponseEntity<List<Brand>> queryBrandsByBrandIds(@RequestParam("bids") List<Long> brandIds){
        List<Brand> brands = this.brandService.queryBrandsByBids(brandIds);
        if (brands==null||brands.size()<1){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.status(HttpStatus.OK).body(brands);
    }

}
