package com.gamesmicroservice.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class JedisConnectionTest {

    @Autowired
    private JedisPool jedisPool;

    @Test
    void testJedisConnection() {
        try (Jedis jedis = jedisPool.getResource()) {
            String result = jedis.set("test:key", "test-value");
            assertEquals("OK", result);

            String value = jedis.get("test:key");
            assertEquals("test-value", value);

            jedis.del("test:key");
        }
    }
}