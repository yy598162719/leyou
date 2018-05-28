package com.leyou.item.controller;

import com.github.pagehelper.Page;
import com.leyou.item.PageResult;
import com.leyou.item.service.BrandService;
import com.leyou.pojo.Brand;
import com.mysql.jdbc.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Qin PengCheng
 * @date 2018/5/28
 */
@RestController
@RequestMapping("brand")
public class BrandController {

        @Autowired
        private BrandService brandService;

    @GetMapping("page")
    public ResponseEntity<PageResult> queryBrandByPage(
            @RequestParam("key") String key,
            @RequestParam(value = "page",defaultValue = "1") int page,
            @RequestParam(value = "rows",defaultValue = "5") int rows,
            @RequestParam("sortBy") String sortBy,
            @RequestParam(value = "desc",defaultValue = "false") Boolean desc
    ) {
        PageResult<Brand> pageResult = brandService.queryBrandByPage(key,page,rows,sortBy,desc);
        if (pageResult==null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(pageResult);
    }
}
