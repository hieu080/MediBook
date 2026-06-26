package com.identityservice.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public final class JwtClaimUtils {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JwtClaimUtils() {
    }

    public static Map<String, Object> payload(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return Map.of();
            }
            byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
            return OBJECT_MAPPER.readValue(new String(decoded, StandardCharsets.UTF_8), new TypeReference<>() {
            });
        } catch (Exception ex) {
            return Map.of();
        }
    }

    public static String stringClaim(String token, String claimName) {
        Object value = payload(token).get(claimName);
        return value == null ? null : value.toString();
    }
}
