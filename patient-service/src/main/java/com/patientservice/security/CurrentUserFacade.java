package com.patientservice.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class CurrentUserFacade {

    public UUID getCurrentUserPublicId() {
        return UUID.fromString(getJwt().getClaimAsString("publicId"));
    }

    public String getCurrentUserEmail() {
        return getJwt().getClaimAsString("email");
    }

    public List<String> getCurrentUserRoles() {
        List<String> roles = getJwt().getClaimAsStringList("roles");
        if (roles != null) {
            return roles;
        }

        Map<String, Object> realmAccess = getJwt().getClaimAsMap("realm_access");
        if (realmAccess != null && realmAccess.get("roles") instanceof List<?> realmRoles) {
            return realmRoles.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
        }

        return List.of();
    }

    public boolean hasRole(String role) {
        String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return getAuthentication().getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority::equals);
    }

    private Jwt getJwt() {
        Object principal = getAuthentication().getPrincipal();
        if (principal instanceof Jwt jwt) {
            return jwt;
        }
        throw new IllegalStateException("Current principal is not a JWT");
    }

    private Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Authentication is required");
        }
        return authentication;
    }
}
