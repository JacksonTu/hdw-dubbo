package com.hdw.web.base.shiro.jwt.filter;


import com.hdw.common.core.utils.JacksonUtil;
import com.hdw.web.base.shiro.jwt.JwtToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * @author JacksonTu
 * @version 1.0.0
 * @Description oauth2过滤器
 * @date 2018年6月11日下午4:49:37
 */
@Slf4j
public class JwtFilter extends AuthenticatingFilter {

    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {
        //获取请求token
        String token = getRequestToken((HttpServletRequest) request);

        if (StringUtils.isBlank(token)) {
            return null;
        }

        return new JwtToken(token);
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        return ((HttpServletRequest) request).getMethod().equals(RequestMethod.OPTIONS.name());
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        //获取请求token，如果token不存在，直接返回401
        String token = getRequestToken((HttpServletRequest) request);

        log.info("===============token: {}============", token);
        if (StringUtils.isBlank(token)) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
            httpResponse.setHeader("Access-Control-Allow-Origin", httpRequest.getHeader("Origin"));

            Map<String, Object> par = new HashMap<>();
            par.put("code", HttpStatus.SC_UNAUTHORIZED);
            par.put("msg", "invalid token");

            httpResponse.getWriter().print(JacksonUtil.toJson(par));

            return false;
        }

        return executeLogin(request, response);
    }

    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        httpResponse.setContentType("application/json;charset=utf-8");
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        httpResponse.setHeader("Access-Control-Allow-Origin", httpRequest.getHeader("Origin"));
        try {
            //处理登录失败的异常
            Throwable throwable = e.getCause() == null ? e : e.getCause();

            Map<String, Object> par = new HashMap<>();
            par.put("code", HttpStatus.SC_UNAUTHORIZED);
            par.put("msg", throwable.getMessage());

            httpResponse.getWriter().print(JacksonUtil.toJson(par));
        } catch (IOException e1) {
            e1.getStackTrace();
        }

        return false;
    }

    /**
     * 获取请求的token
     */
    private String getRequestToken(HttpServletRequest httpRequest) {
        //从header中获取token
        String token = httpRequest.getHeader("token");

        //如果header中不存在token，则从参数中获取token
        if (StringUtils.isBlank(token)) {
            token = httpRequest.getParameter("token");
        }

        return token;
    }
}
