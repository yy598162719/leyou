package com.leyou.auth.service;

import com.leyou.auth.client.UserClient;
import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.entiy.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.user.pojo.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * @author Qin PengCheng
 * @date 2018/6/13
 */
@Service
@EnableConfigurationProperties(JwtProperties.class)
public class AuthService {

    @Autowired
    private UserClient userClient;

    @Autowired
    private JwtProperties jwtProperties;

    private Logger logger = LoggerFactory.getLogger(AuthService.class);

    /**
     *获取令牌的方法
     * @param username
     * @param password
     * @return
     */
    public String getToken(String username, String password) {
        try {
            ResponseEntity<User> userResponseEntity = this.userClient.queryUser(username, password);
            if (!userResponseEntity.hasBody()) {
                logger.info("用户信息不存在，{}", username);
                return null;
            }
            User user = userResponseEntity.getBody();
            //生成令牌
            UserInfo userInfo = new UserInfo();
            BeanUtils.copyProperties(user, userInfo);
            String token = JwtUtils.generateToken(userInfo, jwtProperties.getPrivateKey(), jwtProperties.getExpire());
            return token;
        } catch (Exception e) {
            logger.error("生成令牌的过程中出错");
            return null;
        }
    }
}
