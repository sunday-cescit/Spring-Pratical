package com.gamesmicroservice.rest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisCacheServiceTest {
    private static final String TEST_CACHE_NAME = "testCache";
    private static final String TEST_KEY = "testKey";
    private static final String TEST_VALUE = "testValue";
    private static final String TEST_CACHE_KEY = TEST_CACHE_NAME + ":" + TEST_KEY;

    @Mock private JedisPool jedisPool;
    @Mock private Jedis jedis;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks
    private RedisCacheService redisCacheService;

    @BeforeEach
    void setUp() {
        when(jedisPool.getResource()).thenReturn(jedis);
    }

    @Test
    void get_shouldReturnCachedValue() throws Exception {
        // Arrange
        when(jedis.get(TEST_CACHE_KEY)).thenReturn(TEST_VALUE);
        when(objectMapper.readValue(TEST_VALUE, String.class)).thenReturn(TEST_VALUE);

        // Act
        Optional<String> result = redisCacheService.get(TEST_CACHE_NAME, TEST_KEY, String.class);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(TEST_VALUE, result.get());
    }

    @Test
    void get_shouldReturnEmptyOnCacheMiss() {
        // Arrange
        when(jedis.get(TEST_CACHE_KEY)).thenReturn(null);

        // Act
        Optional<String> result = redisCacheService.get(TEST_CACHE_NAME, TEST_KEY, String.class);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void get_shouldHandleDeserializationError() throws Exception {
        // Arrange
        when(jedis.get(TEST_CACHE_KEY)).thenReturn(TEST_VALUE);
        when(objectMapper.readValue(TEST_VALUE, String.class)).thenThrow(JsonProcessingException.class);

        // Act
        Optional<String> result = redisCacheService.get(TEST_CACHE_NAME, TEST_KEY, String.class);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void get_shouldHandleJedisException() {
        // Arrange
        when(jedis.get(TEST_CACHE_KEY)).thenThrow(JedisException.class);

        // Act
        Optional<String> result = redisCacheService.get(TEST_CACHE_NAME, TEST_KEY, String.class);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void put_shouldCacheValueSuccessfully() throws Exception {
        // Arrange
        when(objectMapper.writeValueAsString(TEST_VALUE)).thenReturn(TEST_VALUE);

        // Act
        redisCacheService.put(TEST_CACHE_NAME, TEST_KEY, TEST_VALUE);

        // Assert - Use long (600L) for TTL instead of integer (600)
        verify(jedis).setex(eq(TEST_CACHE_KEY), eq(600L), eq(TEST_VALUE));
    }


    @Test
    void put_shouldHandleSerializationError() throws Exception {
        // Arrange
        when(objectMapper.writeValueAsString(TEST_VALUE)).thenThrow(JsonProcessingException.class);

        // Act
        redisCacheService.put(TEST_CACHE_NAME, TEST_KEY, TEST_VALUE);

        // Assert
        verify(jedis, never()).setex(anyString(), anyInt(), anyString());
    }

    @Test
    void put_shouldHandleJedisException() throws Exception {
        // Arrange
        when(objectMapper.writeValueAsString(TEST_VALUE)).thenReturn(TEST_VALUE);
        doThrow(JedisException.class).when(jedis).setex(anyString(), anyInt(), anyString());

        // Act
        redisCacheService.put(TEST_CACHE_NAME, TEST_KEY, TEST_VALUE);

        // Assert - No further verification, as we only need to test that the method handles the exception
    }

    @Test
    void evict_shouldRemoveKeySuccessfully() {
        // Act
        redisCacheService.evict(TEST_CACHE_NAME, TEST_KEY);

        // Assert
        verify(jedis).del(TEST_CACHE_KEY);
    }

    @Test
    void evict_shouldHandleJedisException() {
        // Arrange
        doThrow(JedisException.class).when(jedis).del(anyString());

        // Act
        redisCacheService.evict(TEST_CACHE_NAME, TEST_KEY);

        // Assert - Handle exception correctly
    }

    @Test
    void evictAll_shouldRemoveAllKeysForCache() {
        // Arrange
        String pattern = TEST_CACHE_NAME + ":*";

        // Act
        redisCacheService.evictAll(TEST_CACHE_NAME);

        // Assert
        verify(jedis).eval(
            eq("local keys = redis.call('keys', ARGV[1]) " +
               "for i=1,#keys,5000 do " +
               "  redis.call('del', unpack(keys, i, math.min(i+4999, #keys))) " +
               "end " +
               "return keys"),
            eq(Collections.emptyList()),
            eq(Collections.singletonList(pattern))
        );
    }

    @Test
    void evictAll_shouldHandleJedisException() {
        // Arrange
        doThrow(JedisException.class).when(jedis).eval(anyString(), anyList(), anyList());

        // Act
        redisCacheService.evictAll(TEST_CACHE_NAME);

        // Assert - Handle exception correctly
    }

    @Test
    void get_shouldReturnComplexObjects() throws Exception {
        // Arrange
        TestUser user = new TestUser("John", "john@example.com");
        String userJson = "{\"name\":\"John\",\"email\":\"john@example.com\"}";

        when(jedis.get(TEST_CACHE_KEY)).thenReturn(userJson);
        when(objectMapper.readValue(userJson, TestUser.class)).thenReturn(user);

        // Act
        Optional<TestUser> result = redisCacheService.get(TEST_CACHE_NAME, TEST_KEY, TestUser.class);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    static class TestUser {
        private String name;
        private String email;

        public TestUser() {}

        public TestUser(String name, String email) {
            this.name = name;
            this.email = email;
        }

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestUser testUser = (TestUser) o;
            return name.equals(testUser.name) && email.equals(testUser.email);
        }
    }
}
