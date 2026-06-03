package com.identityservice.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * DTO chứa cặp token JWT trả về cho client sau khi xác thực thành công.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */
@Getter
@Builder
public class JwtTokenResponse {
    private String accessToken;
    private String refreshToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private Long expiresIn;
}
