package com.leyou.item.controller;

import com.leyou.item.Bo.GoodsBo;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Qin PengCheng
 * @date 2018/6/2
 */

@RestController
@RequestMapping("goods")
public class GoodsController {

    /*url: "/item/goods",
    method: this.isEdit ? "put" : "post",
    data: this.goods
})*/

    @Autowired
    private GoodsService goodsService;

    @PostMapping
    public ResponseEntity<Void> addGoods(@RequestBody GoodsBo goods){
        this.goodsService.addGoods(goods);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }




}
