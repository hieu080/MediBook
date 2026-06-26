package com.sharekernel.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceTokenResponse {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private long expiresIn;

    @JsonProperty("token_type")
    private String tokenType;

    private String scope;
}
