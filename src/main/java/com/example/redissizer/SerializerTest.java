package com.example.redissizer;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class SerializerTest {

    private final Logger logger = Logger.getLogger(SerializerTest.class.getName());

    private final RedisTemplate<String, Object> redisTemplate;

    public SerializerTest(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public <T> void testSerialization(String key, Object value, RedisSerializer<T> serializer) {
        var start = System.currentTimeMillis();
        redisTemplate.setValueSerializer(serializer);
        redisTemplate.setKeySerializer(StringRedisSerializer.UTF_8);
        redisTemplate.opsForValue().set(key, value);
//        logger.info("Serializing value for key %s took %sms".formatted(key, System.currentTimeMillis() - start));
    }

    public <T> T getObject(String jsonKey, RedisSerializer<T> serializer) {
        redisTemplate.setValueSerializer(serializer);
        return (T) redisTemplate.opsForValue().get(jsonKey);

    }
}