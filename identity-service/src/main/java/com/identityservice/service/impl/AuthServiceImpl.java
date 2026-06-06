package com.identityservice.service.impl;

import com.identityservice.dto.request.LoginRequest;
import com.identityservice.dto.request.RefreshTokenRequest;
import com.identityservice.dto.request.RegisterRequest;
import com.identityservice.dto.response.AuthResponse;
import com.identityservice.dto.response.JwtTokenResponse;
import com.identityservice.dto.response.UserPrivateResponse;
import com.identityservice.entity.RefreshToken;
import com.identityservice.entity.User;
import com.identityservice.enums.UserStatus;
import com.identityservice.exception.AuthErrorCode;
import com.identityservice.exception.IdentityException;
import com.identityservice.exception.UserErrorCode;
import com.identityservice.mapper.AuthMapper;
import com.identityservice.mapper.UserMapper;
import com.identityservice.repository.UserRepository;
import com.identityservice.repository.UserRoleRepository;
import com.identityservice.security.CurrentUserFacade;
import com.identityservice.security.JwtService;
import com.identityservice.service.AuthService;
import com.identityservice.service.RefreshTokenService;
import com.identityservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Trien khai nghiep vu xac thuc co ban cho identity-service.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthMapper authMapper;
    private final UserMapper userMapper;
    private final CurrentUserFacade currentUserFacade;

    @Override
    public UserPrivateResponse register(RegisterRequest request) {
        UserPrivateResponse createdUser = userService.createUser(request);
        User user = userRepository.findByPublicIdAndDeletedAtIsNull(createdUser.getPublicId())
                .orElseThrow(() -> new IdentityException(UserErrorCode.USER_NOT_FOUND));
        return userMapper.toPrivateResponse(user, loadRoleCodes(user));
    }

    @Override
    public AuthResponse login(LoginRequest request, String deviceInfo, String ipAddress) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.getEmail())
                .orElseThrow(() -> new IdentityException(AuthErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IdentityException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        validateUserStatus(user);
        return buildAuthResponse(user, loadRoleCodes(user), deviceInfo, ipAddress);
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request, String deviceInfo, String ipAddress) {
        RefreshToken storedRefreshToken = refreshTokenService.validateRefreshToken(request.getRefreshToken());
        User user = storedRefreshToken.getUser();
        validateUserStatus(user);
        refreshTokenService.revokeRefreshToken(request.getRefreshToken());
        return buildAuthResponse(user, loadRoleCodes(user), deviceInfo, ipAddress);
    }

    @Override
    public void logout(RefreshTokenRequest request) {
        refreshTokenService.revokeRefreshToken(request.getRefreshToken());
    }

    @Override
    public void logoutAll() {
        User user = userRepository.findByIdAndDeletedAtIsNull(currentUserFacade.getCurrentUserId())
                .orElseThrow(() -> new IdentityException(UserErrorCode.USER_NOT_FOUND));
        refreshTokenService.revokeAllByUser(user);
    }

    private AuthResponse buildAuthResponse(User user, List<String> roles, String deviceInfo, String ipAddress) {
        String refreshToken = refreshTokenService.createRefreshToken(user, deviceInfo, ipAddress);
        return buildAuthResponse(user, roles, refreshToken);
    }

    private AuthResponse buildAuthResponse(User user, List<String> roles, String refreshToken) {
        String accessToken = jwtService.generateAccessToken(user, roles);
        JwtTokenResponse jwtTokenResponse = authMapper.toJWTTokenResponse(
                accessToken,
                refreshToken,
                jwtService.getAccessTokenExpiration()
        );
        return authMapper.toAuthResponse(userMapper.toPrivateResponse(user, roles), jwtTokenResponse);
    }

    private List<String> loadRoleCodes(User user) {
        return userRoleRepository.findAllByUserAndDeletedAtIsNull(user)
                .stream()
                .map(userRole -> userRole.getRole().getCode())
                .toList();
    }

    private void validateUserStatus(User user) {
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new IdentityException(UserErrorCode.USER_SUSPENDED);
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IdentityException(UserErrorCode.USER_INACTIVE);
        }
    }
}
