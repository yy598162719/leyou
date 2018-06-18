package com.leyou.cart.controller;

import com.leyou.cart.pojo.Cart;
import com.leyou.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Qin PengCheng
 * @date 2018/6/14
 */

@RequestMapping
@RestController
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping
    public ResponseEntity<Void> addCart(@RequestBody Cart cart) {

        this.cartService.addCart(cart);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping
    public ResponseEntity<List<Cart>> queryCart() {
        List<Cart> list = this.cartService.queryCart();
        if (list == null || list.size() < 1) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    /**
     * 修改购物车商品的数量
     * @param skuId
     * @param num
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> updateCart(@RequestParam("skuId") Long skuId,@RequestParam("num") Integer num){
        this.cartService.updateCart(skuId,num);
        return ResponseEntity.status(HttpStatus.OK).build();
    }


    /**
     * 删除商品
     * @param skuId
     * @return
     */
    @DeleteMapping("{skuId}")
    public ResponseEntity<Void> deleteCart(
            @PathVariable("skuId") String skuId
    ){
        this.cartService.daleteCart(Long.valueOf(skuId));
        return ResponseEntity.status(HttpStatus.OK).build();
    }

   /* @PostMapping
    public ResponseEntity<Void> addCart(
            List<Cart> list
    ){
        for (Cart cart : list) {
        this.cartService.addCart(cart);
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }*/
}

