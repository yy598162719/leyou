package com.leyou.api;



import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author Qin PengCheng
 * @date 2018/6/5
 */
@RequestMapping("category")
public interface CategoryApi {

    /**
     * 根据分类id查询分类名称的方法
     *
     * @param list
     * @return
     */
    @GetMapping("names")
    public ResponseEntity<List<String>> queryCategoryNamesBycids(
            @RequestParam("ids") List<Long> list
    );

}
