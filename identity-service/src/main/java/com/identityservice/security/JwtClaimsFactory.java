package com.identityservice.security;

import com.identityservice.entity.User;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory chuyển đổi thông tin người dùng thành claims cho JWT.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */
@Component
public class JwtClaimsFactory {

    public Map<String, Object> createAccessTokenClaims(User user, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("publicId", user.getPublicId());
        claims.put("email", user.getEmail());
        claims.put("fullName", user.getFullName());
        claims.put("roles", roles);
        return claims;
    }
}
