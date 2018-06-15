package com.leyou.item.controller;

import com.leyou.bo.GoodsBo;
import com.leyou.item.service.GoodsService;
import com.leyou.cart.pojo.Sku;
import com.leyou.cart.pojo.SpuDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Qin PengCheng
 * @date 2018/6/2
 */

@RestController
@RequestMapping("goods")
public class GoodsController {


    @Autowired
    private GoodsService goodsService;

    /**
     * 添加商品
     * @param goods
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> addGoods(@RequestBody GoodsBo goods){
        this.goodsService.addGoods(goods);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    /**
     * 根据id查询商品细节的方法
     * @param id
     * @return
     */
    @GetMapping("spu/detail/{id}")
    public ResponseEntity<SpuDetail> querySpuDetailById(@PathVariable("id")Long id){
        SpuDetail spuDetail = goodsService.querySpuDetailById(id);
        if (spuDetail==null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.status(HttpStatus.OK).body(spuDetail);
    }

    /**
     * 删除商品的方法
     * @param id
     * @return
     */
    @DeleteMapping()
    public ResponseEntity<Void> deleteSpuById(@RequestParam("id") Long id){
                this.goodsService.deleteSpuById(id);
                return ResponseEntity.status(HttpStatus.OK).build();

    }

    /**
     * 更改商品的上下架状态
     * @param id
     * @param selable
     * @return
     */
    @PutMapping("spu")
    public ResponseEntity<Void> updateSealStand(@RequestParam("id")Long id,@RequestParam("selable")Boolean selable){
        this.goodsService.updateSealStand(id,selable);
        return ResponseEntity.status(HttpStatus.OK).build();

    }

    /**
     * 修改商品的方法
     * @param goods
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> updateGoods(@RequestBody GoodsBo goods){
        this.goodsService.updateGoods(goods);
        return ResponseEntity.status(HttpStatus.OK).build();
    }


    /**
     * 查询sku的方法
     * @param id
     * @return
     */
    @GetMapping("/sku/list")
    public ResponseEntity<List<Sku>> querySkuList(@RequestParam("id")Long id){
     List<Sku> skus =   this.goodsService.querySkuList(id);
     return ResponseEntity.status(HttpStatus.OK).body(skus);
    }

    @GetMapping("sku/{id}")
    public ResponseEntity<Sku> querySkuBySkuId(
            @PathVariable("id")Long id
    ){
        Sku sku = this.goodsService.querySkuBySkuId(id);
        if (sku==null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(sku);
    }
}
