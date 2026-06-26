package com.sharekernel.security;

public interface ServiceTokenProvider {
    String getAccessToken();

    default String getAuthorizationHeader() {
        return "Bearer " + getAccessToken();
    }
}
