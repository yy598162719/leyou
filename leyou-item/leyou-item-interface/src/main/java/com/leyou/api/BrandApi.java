package com.leyou.api;

import com.leyou.cart.pojo.Brand;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author Qin PengCheng
 * @date 2018/6/7
 */
@RequestMapping("brand")
public interface BrandApi {

    @GetMapping("bids")
    ResponseEntity<List<Brand>> queryBrandsByBrandIds(@RequestParam("bids") List<Long> brandIds);
}
