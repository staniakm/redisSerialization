package com.example.redissizer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

@Service
public class RedisMemoryUsage {

    @Autowired
    private Jedis jedis;


    public long getMemoryUsage(String key) {
        return jedis.memoryUsage(key);
    }
}
