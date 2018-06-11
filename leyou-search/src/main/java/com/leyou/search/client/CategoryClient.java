package com.leyou.search.client;

import com.leyou.api.CategoryApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author Qin PengCheng
 * @date 2018/6/5
 */
@FeignClient(value = "item-service")
public interface CategoryClient extends CategoryApi {
}
