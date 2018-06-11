package com.leyou.goodsDetail.client;

import com.leyou.api.SpuApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author Qin PengCheng
 * @date 2018/6/5
 */
@FeignClient(value = "item-service")
public interface SpuClient extends SpuApi {
}
