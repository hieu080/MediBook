package com.rediscommon.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisCluster;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RedisCommon {

    private final JedisCluster jedisCluster;
    private final ObjectMapper objectMapper;

    public void set(String key, String value) {
        validateKey(key);
        jedisCluster.set(key, value);
    }

    public void set(String key, String value, Duration ttl) {
        validateKey(key);
        jedisCluster.setex(key, (int) ttl.getSeconds(), value);
    }

    public <T> void setObject(String key, T value) {
        validateKey(key);
        try {
            String json = objectMapper.writeValueAsString(value);
            jedisCluster.set(key, json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot serialize object to JSON", e);
        }
    }

    public <T> void setObject(String key, T value, Duration ttl) {
        validateKey(key);
        try {
            String json = objectMapper.writeValueAsString(value);
            jedisCluster.setex(key, (int) ttl.getSeconds(), json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot serialize object to JSON", e);
        }
    }

    public Optional<String> get(String key) {
        validateKey(key);
        return Optional.ofNullable(jedisCluster.get(key));
    }

    public <T> Optional<T> getObject(String key, Class<T> clazz) {
        validateKey(key);
        String json = jedisCluster.get(key);
        if (!StringUtils.hasText(json)) return Optional.empty();
        try {
            return Optional.of(objectMapper.readValue(json, clazz));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot deserialize JSON from Redis", e);
        }
    }

    public Long delete(String key) {
        validateKey(key);
        return jedisCluster.del(key);
    }

    public Long delete(List<String> keys) {
        if (keys == null || keys.isEmpty()) return 0L;
        long total = 0;
        for (String key : keys) {
            if (StringUtils.hasText(key)) total += jedisCluster.del(key);
        }
        return total;
    }

    public Boolean hasKey(String key) {
        validateKey(key);
        return jedisCluster.exists(key);
    }

    public Long expire(String key, Duration ttl) {
        validateKey(key);
        return jedisCluster.expire(key, (int) ttl.getSeconds());
    }

    public Long getExpire(String key) {
        validateKey(key);
        return jedisCluster.ttl(key);
    }

    private void validateKey(String key) {
        if (!StringUtils.hasText(key)) throw new IllegalArgumentException("Redis key must not be blank");
    }
}