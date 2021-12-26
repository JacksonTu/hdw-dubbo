package com.hdw.common.core.web;

import com.hdw.common.core.utils.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.List;

/**
 * @Description WebMvc配置
 * @Author JacksonTu
 * @Date 2020/4/1 17:35
 */
@Slf4j
public class WebConfigure implements WebMvcConfigurer {
    /**
     * 资源处理器
     *
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
        registry.addResourceHandler("swagger-ui.html", "doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");

    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        for (int i = 0; i < converters.size(); i++) {
            HttpMessageConverter<?> messageConverter = converters.get(i);
            if (messageConverter instanceof MappingJackson2HttpMessageConverter) {
                converters.remove(i);
            }
        }
        MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        jackson2HttpMessageConverter.setObjectMapper(JacksonUtil.getObjectMapper());

        //放到第一个
        converters.add(jackson2HttpMessageConverter);
    }
}
