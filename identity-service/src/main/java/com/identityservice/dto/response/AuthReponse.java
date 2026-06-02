package com.identityservice.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

/**
 * DTO phản hồi sau khi người dùng đăng nhập hoặc đăng ký thành công.
 * Chứa thông tin về token truy cập và thông tin người dùng.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */

@Getter
@Builder
public class AuthReponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private UUID publicId;
    private String fullName;
    private String email;
    private Long expiresIn; // Thời gian sống của token (tính bằng giây)
    private List<String> roles; // Danh sách vai trò của người dùng
}
