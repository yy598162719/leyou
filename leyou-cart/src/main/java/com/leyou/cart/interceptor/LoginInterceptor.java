package com.leyou.cart.interceptor;

import com.leyou.auth.entiy.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.cart.config.JwtProperties;
import com.leyou.utils.CookieUtils;
import com.sun.istack.internal.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Qin PengCheng
 * @date 2018/6/13
 */
public class LoginInterceptor extends HandlerInterceptorAdapter {

    private JwtProperties jwtProperties;

    //一个线程域，存放登陆的对象
    private static final ThreadLocal<UserInfo> t_user = new ThreadLocal<>();

    public LoginInterceptor(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    //获取登陆用户的方法
    public static UserInfo getUserInfo() {
        return t_user.get();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            //首先拿到令牌
            String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());
            //判断令牌是否是空
            if (StringUtils.isBlank(token)) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                return false;
            }
            //令牌不为空，解析得到用户的信息
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
            t_user.set(userInfo);
            return true;
        } catch (Exception e) {
            //抛出异常，证明未登陆，401
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        t_user.remove();
    }

}
