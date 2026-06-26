package com.identityservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakProperties {
    private String issuerUri;
    private String tokenUri;
    private String logoutUri;
    private String adminBaseUrl;
    private String realm;
    private String clientId;
    private String clientSecret;
}
