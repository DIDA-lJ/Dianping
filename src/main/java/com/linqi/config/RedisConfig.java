package com.linqi.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author linqi
 * @version 1.0.0
 * @description
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedissonClient redissonClient(){
        // 配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.88.101:6379").setPassword("123456");
        // 创建RedissonClient对象
        return Redisson.create(config);
    }

}
