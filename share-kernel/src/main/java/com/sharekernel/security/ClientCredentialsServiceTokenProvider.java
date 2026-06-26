package com.sharekernel.security;

import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.time.Clock;
import java.time.Instant;

public class ClientCredentialsServiceTokenProvider implements ServiceTokenProvider {
    private final ServiceTokenProperties properties;
    private final RestClient restClient;
    private final Clock clock;

    private String cachedAccessToken;
    private Instant expiresAt = Instant.EPOCH;

    public ClientCredentialsServiceTokenProvider(ServiceTokenProperties properties) {
        this(properties, RestClient.create(), Clock.systemUTC());
    }

    public ClientCredentialsServiceTokenProvider(ServiceTokenProperties properties, RestClient restClient, Clock clock) {
        this.properties = properties;
        this.restClient = restClient;
        this.clock = clock;
    }

    @Override
    public synchronized String getAccessToken() {
        if (cachedAccessToken != null && Instant.now(clock).isBefore(expiresAt)) {
            return cachedAccessToken;
        }

        ServiceTokenResponse response = requestToken();
        cachedAccessToken = response.getAccessToken();
        long cacheSeconds = Math.max(0, response.getExpiresIn() - properties.getRefreshSkewSeconds());
        expiresAt = Instant.now(clock).plusSeconds(cacheSeconds);
        return cachedAccessToken;
    }

    private ServiceTokenResponse requestToken() {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", properties.getClientId());
        body.add("client_secret", properties.getClientSecret());

        return restClient.post()
                .uri(properties.getTokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(ServiceTokenResponse.class);
    }
}
