package com.gamesmicroservice.rest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class RedisConfig {

    @Bean(destroyMethod = "close")
    public JedisPool jedisPool() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(10);
        poolConfig.setMaxIdle(5);
        poolConfig.setMinIdle(1);
        poolConfig.setTestOnBorrow(true);

        String host = "redis-17497.c92.us-east-1-3.ec2.redns.redis-cloud.com";
        int port = 17497;
        String password = "HX2rErIlX7LrwPYlbxRSFAI19ei6Hhfs";

        return new JedisPool(poolConfig, host, port, 2000, password);
    }
}
