package com.gamesmicroservice.rest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Optional;

@Service
public class RedisCacheService {
    private static final int TTL_SECONDS = 600; // 10 minutes
    
    private final JedisPool jedisPool;
    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(RedisCacheService.class);

    public RedisCacheService(JedisPool jedisPool, ObjectMapper objectMapper) {
        this.jedisPool = jedisPool;
        this.objectMapper = objectMapper;
    }

    public <T> Optional<T> get(String cacheName, String key, Class<T> type) {
        String cacheKey = buildCacheKey(cacheName, key);
        try (Jedis jedis = jedisPool.getResource()) {
            String cachedValue = jedis.get(cacheKey);
            if (cachedValue != null) {
                logger.info("Cache hit for key: {}", cacheKey);
                return Optional.of(objectMapper.readValue(cachedValue, type));
            } else {
                logger.info("Cache miss for key: {}", cacheKey);
                return Optional.empty();
            }
        } catch (JsonProcessingException e) {
            logger.error("Failed to deserialize cache value for key: {}", cacheKey, e);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error accessing cache for key: {}", cacheKey, e);
            return Optional.empty();
        }
    }
    

    public void put(String cacheName, String key, Object value) {
        String cacheKey = buildCacheKey(cacheName, key);
        try (Jedis jedis = jedisPool.getResource()) {
            String jsonValue = objectMapper.writeValueAsString(value);
            jedis.setex(cacheKey, TTL_SECONDS, jsonValue);
            logger.info("Cached value for key: {} (TTL: {}s)", cacheKey, TTL_SECONDS);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize value for caching (key: {})", cacheKey, e);
        } catch (Exception e) {
            logger.error("Error writing to cache for key: {}", cacheKey, e);
        }
    }
    

    public void evict(String cacheName, String key) {
        String cacheKey = buildCacheKey(cacheName, key);
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(cacheKey);
            logger.info("Evicted cache key: {}", cacheKey);
        } catch (Exception e) {
            logger.error("Failed to evict cache key: {}", cacheKey, e);
        }
    }
    

    public void evictAll(String cacheName) {
        String pattern = buildCacheKey(cacheName, "*");
        try (Jedis jedis = jedisPool.getResource()) {
            Object result = jedis.eval(
                "local keys = redis.call('keys', ARGV[1]) " +
                "for i=1,#keys,5000 do " +
                "  redis.call('del', unpack(keys, i, math.min(i+4999, #keys))) " +
                "end " +
                "return keys",
                Collections.emptyList(),
                Collections.singletonList(pattern)
            );
            logger.info("Evicted all keys matching pattern: {}", pattern);
        } catch (Exception e) {
            logger.error("Failed to evict keys matching pattern: {}", pattern, e);
        }
    }    

    private String buildCacheKey(String cacheName, String key) {
        return String.format("%s:%s", cacheName, key);
    }
}