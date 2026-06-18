package com.rediscommon.config;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Connection;

public class JedisConfigCommon {

    private JedisConfigCommon() {}

    public static GenericObjectPoolConfig<Connection> buildPoolConfig(RedisNodes redisNodes) {
        GenericObjectPoolConfig<Connection> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(redisNodes.getMaxTotal());
        poolConfig.setMaxIdle(redisNodes.getMaxIdle());
        poolConfig.setMinIdle(redisNodes.getMinIdle());
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(false);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setBlockWhenExhausted(true);
        return poolConfig;
    }
}
