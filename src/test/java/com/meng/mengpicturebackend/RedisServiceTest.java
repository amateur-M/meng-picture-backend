package com.meng.mengpicturebackend;

import com.meng.mengpicturebackend.service.RedisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * redis操作测试类
 */
@SpringBootTest
public class RedisServiceTest {

    @Autowired
    private RedisService redisService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    public void testSetValueAndGet() {
        String key = "testKey";
        String value = "testValue";

        // Set value in Redis
        redisService.setValue(key, value);

        // Get value from Redis
        String retrievedValue = redisService.getValue(key);

        // Assert that the retrieved value matches the set value
        assertEquals(value, retrievedValue);
    }

    @Test
    public void testDeleteValue() {
        String key = "testKey";
        String value = "testValueDelete";

        // Set value in Redis
//        redisTemplate.opsForValue().set(key, value);

        // Delete value from Redis
        redisTemplate.delete(key);

        // Get value from Redis
        String retrievedValue = redisTemplate.opsForValue().get(key);

        // Assert that the value is null after deletion
        assertEquals(null, retrievedValue);
    }
}
