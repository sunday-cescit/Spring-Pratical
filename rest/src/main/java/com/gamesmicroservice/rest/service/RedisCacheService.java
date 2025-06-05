package com.gamesmicroservice.rest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Collections;
import java.util.Optional;

@Service
public class RedisCacheService {
    private static final int TTL_SECONDS = 600; // 10 minutes
    
    private final JedisPool jedisPool;
    private final ObjectMapper objectMapper;

    public RedisCacheService(JedisPool jedisPool, ObjectMapper objectMapper) {
        this.jedisPool = jedisPool;
        this.objectMapper = objectMapper;
    }

    public <T> Optional<T> get(String cacheName, String key, Class<T> type) {
        try (Jedis jedis = jedisPool.getResource()) {
            String cacheKey = buildCacheKey(cacheName, key);
            String cachedValue = jedis.get(cacheKey);
            if (cachedValue != null) {
                return Optional.of(objectMapper.readValue(cachedValue, type));
            }
            return Optional.empty();
        } catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }

    public void put(String cacheName, String key, Object value) {
        try (Jedis jedis = jedisPool.getResource()) {
            String cacheKey = buildCacheKey(cacheName, key);
            String jsonValue = objectMapper.writeValueAsString(value);
            jedis.setex(cacheKey, TTL_SECONDS, jsonValue);
        } catch (JsonProcessingException e) {
            // Log error
        }
    }

    public void evict(String cacheName, String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(buildCacheKey(cacheName, key));
        }
    }

    public void evictAll(String cacheName) {
        try (Jedis jedis = jedisPool.getResource()) {
            String pattern = buildCacheKey(cacheName, "*");
            jedis.eval(
                "local keys = redis.call('keys', ARGV[1]) " +
                "for i=1,#keys,5000 do " +
                "  redis.call('del', unpack(keys, i, math.min(i+4999, #keys))) " +
                "end " +
                "return keys",
                Collections.emptyList(),
                Collections.singletonList(pattern)
            );
        }
    }

    private String buildCacheKey(String cacheName, String key) {
        return String.format("%s:%s", cacheName, key);
    }
}