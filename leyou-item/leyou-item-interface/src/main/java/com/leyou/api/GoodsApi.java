package com.leyou.api;

import com.leyou.cart.pojo.Sku;
import com.leyou.cart.pojo.SpuDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author Qin PengCheng
 * @date 2018/6/5
 */

@RequestMapping("goods")
public interface GoodsApi {

    /**
     * 根据id查询商品细节的方法
     *
     * @param id
     * @return
     */
    @GetMapping("spu/detail/{id}")
    ResponseEntity<SpuDetail> querySpuDetailById(@PathVariable("id") Long id);


    /**
     * 查询sku的方法
     *
     * @param id
     * @return
     */
    @GetMapping("/sku/list")
    ResponseEntity<List<Sku>> querySkuList(@RequestParam("id") Long id);

    /**
     *查询sku的信息
     * @param id
     * @return
     */
    @GetMapping("sku/{id}")
    public ResponseEntity<Sku> querySkuBySkuId(
            @PathVariable("id")Long id
    );
}
