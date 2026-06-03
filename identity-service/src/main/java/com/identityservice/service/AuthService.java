package com.identityservice.service;

import com.identityservice.dto.request.LoginRequest;
import com.identityservice.dto.request.RefreshTokenRequest;
import com.identityservice.dto.request.RegisterRequest;
import com.identityservice.dto.response.AuthResponse;
import com.identityservice.dto.response.UserResponse;

/**
 * Service xu ly cac nghiep vu dang ky, dang nhap va cap lai JWT.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */
public interface AuthService {
    UserResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request, String deviceInfo, String ipAddress);
    AuthResponse refreshToken(RefreshTokenRequest request, String deviceInfo, String ipAddress);
    void logout(RefreshTokenRequest request);
    void logoutAll();
}
