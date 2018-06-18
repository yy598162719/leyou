package com.leyou.auth.controller;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.entiy.UserInfo;
import com.leyou.auth.service.AuthService;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.utils.CookieUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Qin PengCheng
 * @date 2018/6/13
 */
@RestController
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登陆获取令牌的方法
     * @param username
     * @param password
     * @param request
     * @param response
     * @return
     */
    @PostMapping("accredit")
    public ResponseEntity<Void> authorization(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpServletRequest request,
            HttpServletResponse response
            ){
        String token = this.authService.getToken(username,password);
        if (StringUtils.isBlank(token)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        //将令牌放到cookie中,httponly设置为true，防止js修改
        CookieUtils.setCookie(request,response,jwtProperties.getCookieName(),token,jwtProperties.getCookieMaxAge(),null,true);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * 用户操作，需要刷新token
     * @param token
     * @param request
     * @param response
     * @return
     */
    @GetMapping("verify")
    public ResponseEntity<UserInfo> getUserInfo(
            @CookieValue("LY_TOKEN") String token,
            HttpServletRequest request,
            HttpServletResponse response
    ){
        try {
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
            //只要用户有这个请求，就给用户一个新的token，防止过期
            String newToken = JwtUtils.generateToken(userInfo, jwtProperties.getPrivateKey(), jwtProperties.getExpire());
            //这里的true谁不允许js操作的意思
            CookieUtils.setCookie(request,response,jwtProperties.getCookieName(),
                    newToken,jwtProperties.getCookieMaxAge(),null,true);
            return ResponseEntity.status(HttpStatus.OK).body(userInfo);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
