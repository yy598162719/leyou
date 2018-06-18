package com.leyou.cart.service;

import com.leyou.api.GoodsApi;
import com.leyou.auth.entiy.UserInfo;
import com.leyou.cart.client.GoodsClient;
import com.leyou.cart.interceptor.LoginInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.cart.pojo.Sku;
import com.leyou.utils.JsonUtils;
import org.apache.catalina.User;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.persistence.Id;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Qin PengCheng
 * @date 2018/6/14
 */
@Service
public class CartService {


    //查询sku的信息
    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    //redis的前缀
    static final String KEY_PREFIX = "ly:cart:uid:";

    static final Logger logger = LoggerFactory.getLogger(CartService.class);


    /**
     * 添加购物车
     *
     * @param cart
     */
    public void addCart(Cart cart) {
        //凡事走到这里的，都是登录过的
        //用户名我们在token中已经解析过了
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String key = KEY_PREFIX + userInfo.getId();
        //通过key获取redis中的数据
        BoundHashOperations<String, Object, Object> cartsData = redisTemplate.boundHashOps(key);
        Long skuId = cart.getSkuId();
        Integer num = cart.getNum();
        Boolean boo = cartsData.hasKey(skuId.toString());
        //如果购物车中存在此商品
        if (boo) {
            //修改购物车中商品的数量
            //1.首先从redis中得到该商品
            String json = cartsData.get(skuId.toString()).toString();
            //将得到的数据解析成对象
            Cart redisCart = JsonUtils.parse(json, Cart.class);
            //修改对象中商品的数量
            redisCart.setNum(redisCart.getNum() + num);
        } else {
            //根据skuid查询商品的详细信息
            ResponseEntity<Sku> skuResp = this.goodsClient.querySkuBySkuId(skuId);
            if (!skuResp.hasBody()) {
                logger.error("没有查到相关sku的信息:{}" + skuId);
                throw new RuntimeException();
            }
            //将信息和数量封装在cart中
            Sku skuBody = skuResp.getBody();
            cart.setImage(StringUtils.isBlank(skuBody.getImages()) ? "" : StringUtils.split(skuBody.getImages(), ",")[0]);
            cart.setOwnSpec(skuBody.getOwnSpec());
            cart.setPrice(skuBody.getPrice());
            cart.setTitle(skuBody.getTitle());
            cart.setUserId(userInfo.getId());
        }
        //将此商品添加到redis中
        cartsData.put(skuId.toString(), JsonUtils.serialize(cart));
    }

    /**
     * 查询购物车
     *
     * @return
     */
    public List<Cart> queryCart() {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Long userId = userInfo.getId();
        String key = KEY_PREFIX + userId;
        if (!this.redisTemplate.hasKey(key)) {
            return null;
        }
        BoundHashOperations<String, Object, Object> cartData = this.redisTemplate.boundHashOps(key);
        List<Object> values = cartData.values();
        if (CollectionUtils.isEmpty(values)) {
            return null;
        }
        List<Cart> carts = new ArrayList<>();
        for (Object value : values) {
            Cart c = JsonUtils.parse(value.toString(), Cart.class);
            carts.add(c);
        }
        return carts;
    }

    /**
     * 修改购物车的方法
     *
     * @param cart
     */
    public void updateCart(Long skuId, Integer num) {
        //得到用户的信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Long userId = userInfo.getId();
        String key = KEY_PREFIX + userId;
        //根据key查询到数据
        if (this.redisTemplate.hasKey(key)) {
            BoundHashOperations<String, Object, Object> cartData = this.redisTemplate.boundHashOps(key);
            String json = cartData.get(skuId.toString()).toString();
            Cart redisCart = JsonUtils.parse(json, Cart.class);
            redisCart.setNum(num);
            cartData.put(skuId.toString(), JsonUtils.serialize(redisCart));
        } else {
            logger.error("您所修改的商品不存在");
        }
    }

    /**
     * 删除购物车
     * @param skuId
     */
    public void daleteCart(Long skuId) {
        //首先得到登陆用户的信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Long userId = userInfo.getId();
        String key = KEY_PREFIX + userId;
        Boolean boo = this.redisTemplate.hasKey(key);
        if (boo) {
            BoundHashOperations<String, Object, Object> cartData = this.redisTemplate.boundHashOps(key);
                cartData.delete(skuId.toString());
        }
    }
}
