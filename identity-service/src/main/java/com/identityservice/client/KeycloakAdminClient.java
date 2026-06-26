package com.identityservice.client;

import com.identityservice.config.KeycloakProperties;
import com.identityservice.entity.User;
import com.identityservice.exception.AuthErrorCode;
import com.identityservice.exception.IdentityException;
import com.identityservice.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class KeycloakAdminClient {
    private final KeycloakProperties properties;
    private final KeycloakAuthClient authClient;
    private final RestClient restClient = RestClient.create();

    public String createUser(User user, String password, List<String> roles) {
        KeycloakTokenResponse adminToken = authClient.clientCredentials();
        Map<String, Object> payload = Map.of(
                "username", user.getEmail(),
                "email", user.getEmail(),
                "enabled", true,
                "emailVerified", true,
                "attributes", keycloakAttributes(user),
                "credentials", List.of(Map.of(
                        "type", "password",
                        "value", password,
                        "temporary", false
                ))
        );

        try {
            URI location = restClient.post()
                    .uri(adminUsersUri())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken.getAccessToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity()
                    .getHeaders()
                    .getLocation();
            if (location == null) {
                throw new IdentityException(UserErrorCode.USER_ALREADY_EXISTS, "Keycloak không trả Location cho user mới");
            }
            String path = location.getPath();
            String keycloakUserId = path.substring(path.lastIndexOf('/') + 1);
            updateUserProfile(adminToken.getAccessToken(), keycloakUserId, user);
            assignRealmRoles(adminToken.getAccessToken(), keycloakUserId, roles);
            return keycloakUserId;
        } catch (RestClientException ex) {
            throw new IdentityException(UserErrorCode.USER_ALREADY_EXISTS, "Không thể tạo user trong Keycloak");
        }
    }

    private void updateUserProfile(String adminAccessToken, String keycloakUserId, User user) {
        Map<String, Object> payload = Map.of(
                "username", user.getEmail(),
                "email", user.getEmail(),
                "enabled", true,
                "emailVerified", true,
                "attributes", keycloakAttributes(user)
        );

        restClient.put()
                .uri(adminUsersUri() + "/" + keycloakUserId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity();
    }

    private Map<String, List<String>> keycloakAttributes(User user) {
        return Map.of(
                "publicId", List.of(user.getPublicId().toString()),
                "fullName", List.of(user.getFullName())
        );
    }

    public void logoutUserSessions(String keycloakUserId) {
        KeycloakTokenResponse adminToken = authClient.clientCredentials();
        try {
            restClient.post()
                    .uri(adminUsersUri() + "/" + keycloakUserId + "/logout")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken.getAccessToken())
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            throw new IdentityException(AuthErrorCode.INVALID_CREDENTIALS, "Không thể đăng xuất tất cả phiên Keycloak");
        }
    }

    private void assignRealmRoles(String adminAccessToken, String keycloakUserId, List<String> roles) {
        List<Map<String, Object>> roleRepresentations = roles.stream()
                .map(role -> loadRealmRole(adminAccessToken, role))
                .toList();
        if (roleRepresentations.isEmpty()) {
            return;
        }
        restClient.post()
                .uri(adminUsersUri() + "/" + keycloakUserId + "/role-mappings/realm")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(roleRepresentations)
                .retrieve()
                .toBodilessEntity();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadRealmRole(String adminAccessToken, String role) {
        return restClient.get()
                .uri(properties.getAdminBaseUrl() + "/admin/realms/" + properties.getRealm() + "/roles/" + role)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminAccessToken)
                .retrieve()
                .body(Map.class);
    }

    private String adminUsersUri() {
        return properties.getAdminBaseUrl() + "/admin/realms/" + properties.getRealm() + "/users";
    }
}
