package com.leyou.auth.client;

import com.leyou.user.api.UserApi;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author Qin PengCheng
 * @date 2018/6/13
 */
@FeignClient(value = "user-service")
public interface UserClient extends UserApi{
}
