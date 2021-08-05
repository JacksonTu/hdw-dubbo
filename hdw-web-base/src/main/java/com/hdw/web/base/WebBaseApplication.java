package com.hdw.web.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.EnableAspectJAutoProxy;


/**
 * @author JacksonTu
 * @description Application
 * @date 2017年9月5日下午8:55:08
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableAspectJAutoProxy
public class WebBaseApplication extends SpringBootServletInitializer {

    protected final static Logger logger = LoggerFactory.getLogger(WebBaseApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(WebBaseApplication.class, args);
        logger.info("----WebBaseApplication start----");
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        logger.info("----WebBaseApplication start----");
        return application.sources(WebBaseApplication.class);

    }

}
