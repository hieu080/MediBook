package com.sharekernel.security;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceTokenProperties {
    private String tokenUri;
    private String clientId;
    private String clientSecret;
    private long refreshSkewSeconds = 30;
}
