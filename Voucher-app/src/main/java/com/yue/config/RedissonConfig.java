/**
 * @description:
 * @author: 29874
 * @date: 2025/11/15 15:45
 */

package com.yue.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class  RedissonConfig {

    @Bean
    public RedissonClient redissonClient(){
        // 配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://106.12.79.165:16379");
        // 创建RedissonClient对象
        return Redisson.create(config);
    }
}
