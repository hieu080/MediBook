package com.identityservice.service;

import com.identityservice.entity.RefreshToken;
import com.identityservice.entity.User;

/**
 * Service quản lý refresh token được lưu trong cơ sở dữ liệu.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */
public interface RefreshTokenService {
    String createRefreshToken(User user, String deviceInfo, String ipAddress);
    RefreshToken validateRefreshToken(String refreshToken);
    void revokeRefreshToken(String refreshToken);
    void revokeAllByUser(User user);
}
