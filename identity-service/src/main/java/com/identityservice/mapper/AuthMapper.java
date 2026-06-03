package com.identityservice.mapper;

import com.identityservice.dto.response.AuthResponse;
import com.identityservice.dto.response.JwtTokenResponse;
import com.identityservice.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper chuyển đổi dữ liệu xác thực giữa token payload và DTO phản hồi.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */
@Component
public class AuthMapper {

    public JwtTokenResponse toJWTTokenResponse(String accessToken, String refreshToken, Long expiresIn) {
        return JwtTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .build();
    }

    public AuthResponse toAuthResponse(User user, List<String> roles, JwtTokenResponse tokenResponse) {
        return AuthResponse.builder()
                .token(tokenResponse)
                .publicId(user.getPublicId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .roles(roles)
                .build();
    }
}
