package com.identityservice.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * DTO phan hoi sau khi nguoi dung dang nhap hoac cap lai token thanh cong.
 */
@Getter
@Builder
public class AuthResponse {
    private JwtTokenResponse token;
    private UserPrivateResponse user;
}
