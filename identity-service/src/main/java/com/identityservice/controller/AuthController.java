package com.identityservice.controller;

import com.identityservice.dto.request.LoginRequest;
import com.identityservice.dto.request.RefreshTokenRequest;
import com.identityservice.dto.request.RegisterRequest;
import com.identityservice.dto.response.AuthResponse;
import com.identityservice.dto.response.UserResponse;
import com.identityservice.service.AuthService;
import com.sharekernel.response.ApiResponse;
import com.sharekernel.web.RequestContextUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller cung cap cac API xac thuc co ban cho identity-service.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@Valid @RequestBody RegisterRequest request,
                                              @RequestHeader(value = "User-Agent", required = false) String userAgent,
                                              HttpServletRequest httpServletRequest) {
        UserResponse response = authService.register(request);
        return ApiResponse.success(response, "Đăng ký thành công", RequestContextUtils.getRequestId(httpServletRequest));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                           @RequestHeader(value = "User-Agent", required = false) String userAgent,
                                           HttpServletRequest httpServletRequest) {
        AuthResponse response = authService.login(request, userAgent, httpServletRequest.getRemoteAddr());
        return ApiResponse.success(response, "Đăng nhập thành công", RequestContextUtils.getRequestId(httpServletRequest));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request,
                                                  @RequestHeader(value = "User-Agent", required = false) String userAgent,
                                                  HttpServletRequest httpServletRequest) {
        AuthResponse response = authService.refreshToken(request, userAgent, httpServletRequest.getRemoteAddr());
        return ApiResponse.success(response, "Cấp lại access token thành công", RequestContextUtils.getRequestId(httpServletRequest));
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout(@Valid @RequestBody RefreshTokenRequest request,
                                      HttpServletRequest httpServletRequest) {
        authService.logout(request);
        return ApiResponse.success("OK", "Đăng xuất thành công", RequestContextUtils.getRequestId(httpServletRequest));
    }

    @PostMapping("/logout-all")
    public ApiResponse<String> logoutAll(HttpServletRequest httpServletRequest) {
        authService.logoutAll();
        return ApiResponse.success("OK", "Đăng xuất tất cả phiên thành công", RequestContextUtils.getRequestId(httpServletRequest));
    }
}
