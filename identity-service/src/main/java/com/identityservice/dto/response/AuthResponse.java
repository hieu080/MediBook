package com.identityservice.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

/**
 * DTO phan hoi sau khi nguoi dung dang nhap hoac dang ky thanh cong.
 * Chua thong tin token truy cap va thong tin nguoi dung.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */
@Getter
@Builder
public class AuthResponse {
    private JwtTokenResponse token;
    private UUID publicId;
    private String fullName;
    private String email;
    private List<String> roles;
}
