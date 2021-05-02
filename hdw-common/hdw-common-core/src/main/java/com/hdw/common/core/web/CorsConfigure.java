package com.hdw.common.core.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * @Description 跨域配置
 * @Author JacksonTu
 * @Date 2019/11/1 12:03
 */
@Slf4j
public class CorsConfigure {

    private long maxAge = 60 * 60 * 24 * 7;

    private CorsConfiguration buildConfig() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        // 1 设置访问源地址
        corsConfiguration.addAllowedOriginPattern("*");
        // 2 设置访问源请求头
        corsConfiguration.addAllowedHeader("*");
        // 3 设置访问源请求方法
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.setMaxAge(maxAge);
        corsConfiguration.setAllowCredentials(true);
        return corsConfiguration;
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 4 对接口配置跨域设置
        source.registerCorsConfiguration("/**", buildConfig());
        return new CorsFilter(source);
    }
}
