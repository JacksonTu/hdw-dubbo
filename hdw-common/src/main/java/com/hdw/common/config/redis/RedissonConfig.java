package com.hdw.common.config.redis;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

/**
 * @Description Redisson 配置
 * @Author TuMinglong
 * @Date 2019/2/18 15:23
 **/
@Configuration
public class RedissonConfig {

    @Value("${hdw.redisson.model}")
    private String redissonModel;

    /**
     * redisson客户端
     *
     * @return
     * @throws IOException
     */
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() throws IOException {
        Config config = null;
        if ("single".equals(redissonModel)) {
            config = Config.fromYAML(new ClassPathResource("redisson-single-config.yml").getInputStream());
        } else if ("cluster".equals(redissonModel)) {
            config = Config.fromYAML(new ClassPathResource("redisson-cluster-config.yml").getInputStream());
        }
        return Redisson.create(config);
    }

}
