package com.identityservice.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * DTO phản hồi lại sau khi người dùng đăng nhập hoặc cấp lại token thành công.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */
@Getter
@Builder
public class AuthResponse {
    private JwtTokenResponse token;
    private UserPrivateResponse user;
}
