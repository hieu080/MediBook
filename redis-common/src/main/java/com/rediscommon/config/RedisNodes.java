package com.rediscommon.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "redis.cluster")
public class RedisNodes {

    private List<String> nodes = new ArrayList<>();

    private String password;

    private Integer connectionTimeout;

    private Integer soTimeout;

    private Integer maxAttempts;

    private Integer maxTotal;

    private Integer maxIdle;

    private Integer minIdle;
}
