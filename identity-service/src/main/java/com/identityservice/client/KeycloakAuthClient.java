package com.identityservice.client;

import com.identityservice.config.KeycloakProperties;
import com.identityservice.exception.AuthErrorCode;
import com.identityservice.exception.IdentityException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class KeycloakAuthClient {
    private final KeycloakProperties properties;
    private final RestClient restClient = RestClient.create();

    public KeycloakTokenResponse login(String username, String password) {
        MultiValueMap<String, String> body = baseClientBody();
        body.add("grant_type", "password");
        body.add("username", username);
        body.add("password", password);
        return requestToken(body);
    }

    public KeycloakTokenResponse refresh(String refreshToken) {
        MultiValueMap<String, String> body = baseClientBody();
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", refreshToken);
        return requestToken(body);
    }

    public KeycloakTokenResponse clientCredentials() {
        MultiValueMap<String, String> body = baseClientBody();
        body.add("grant_type", "client_credentials");
        return requestToken(body);
    }

    public void logout(String refreshToken) {
        MultiValueMap<String, String> body = baseClientBody();
        body.add("refresh_token", refreshToken);
        try {
            restClient.post()
                    .uri(properties.getLogoutUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            throw new IdentityException(AuthErrorCode.INVALID_CREDENTIALS, "Không thể đăng xuất khỏi Keycloak");
        }
    }

    private KeycloakTokenResponse requestToken(MultiValueMap<String, String> body) {
        try {
            return restClient.post()
                    .uri(properties.getTokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .body(KeycloakTokenResponse.class);
        } catch (RestClientException ex) {
            throw new IdentityException(AuthErrorCode.INVALID_CREDENTIALS);
        }
    }

    private MultiValueMap<String, String> baseClientBody() {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", properties.getClientId());
        body.add("client_secret", properties.getClientSecret());
        return body;
    }
}
