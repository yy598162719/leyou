package com.leyou.cart.client;

import com.leyou.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author Qin PengCheng
 * @date 2018/6/14
 */
@FeignClient(value = "item-service")
public interface GoodsClient extends GoodsApi {
}
