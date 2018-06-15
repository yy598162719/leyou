package com.leyou.filter;

import com.leyou.auth.utils.JwtUtils;
import com.leyou.cart.config.FilterProperties;
import com.leyou.cart.config.JwtProperties;
import com.leyou.utils.CookieUtils;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author Qin PengCheng
 * @date 2018/6/13
 */

@Component
@EnableConfigurationProperties(value = {JwtProperties.class, FilterProperties.class})
public class LoginFilter extends ZuulFilter {

    private Logger logger = LoggerFactory.getLogger(LoginFilter.class);

    @Autowired
    private FilterProperties filterProperties;

    @Autowired
    private JwtProperties jwtProperties;

    private Boolean isAllowPath(String uri){
        List<String> allowPaths = filterProperties.getAllowPaths();
        boolean isFind = false;
        for (String allowPath : allowPaths) {
            if (uri.startsWith(allowPath)){
             isFind = true;
             break;
            }
        }
        return isFind;
    }

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 5;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        String requestURI = request.getRequestURI();
        return !isAllowPath(requestURI);

    }

    @Override
    public Object run() throws ZuulException {
        //获取上下文
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        String token = CookieUtils.getCookieValue(request,jwtProperties.getCookieName());

        try {
            JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
        } catch (Exception e) {
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(403);
        }
        return null;
    }
}
