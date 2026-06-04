package com.identityservice.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Thuộc tính cấu hình cho JWT trong identity-service.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {
    private String keyId = "identity-service-key";
    private String privateKey;
    private String publicKey;
    private String privateKeyPath;
    private String publicKeyPath;
    private long accessTokenExpiration;
    private long refreshTokenExpiration;
    private String issuer;
    private String headerName;
    private String tokenPrefix;
}
