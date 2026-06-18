package com.rediscommon.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.HashSet;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(RedisNodes.class)
public class RedisClusterConfig {

    private final RedisNodes redisNodes;

    @Bean(destroyMethod = "close")
    public JedisCluster jedisCluster() {
        Set<HostAndPort> clusterNodes = new HashSet<>();
        for (String node : redisNodes.getNodes()) {
            String[] parts = node.split(":");
            if (parts.length != 2) throw new IllegalArgumentException("Invalid redis node: " + node);
            clusterNodes.add(new HostAndPort(parts[0], Integer.parseInt(parts[1])));
        }

        if (StringUtils.hasText(redisNodes.getPassword())) {
            return new JedisCluster(
                    clusterNodes,
                    redisNodes.getConnectionTimeout(),
                    redisNodes.getSoTimeout(),
                    redisNodes.getMaxAttempts(),
                    redisNodes.getPassword(),
                    JedisConfigCommon.buildPoolConfig(redisNodes)
            );
        }

        return new JedisCluster(
                clusterNodes,
                redisNodes.getConnectionTimeout(),
                redisNodes.getSoTimeout(),
                redisNodes.getMaxAttempts(),
                JedisConfigCommon.buildPoolConfig(redisNodes)
        );
    }
}
