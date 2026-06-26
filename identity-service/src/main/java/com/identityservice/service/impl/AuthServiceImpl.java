package com.identityservice.service.impl;

import com.identityservice.client.KeycloakAdminClient;
import com.identityservice.client.KeycloakAuthClient;
import com.identityservice.client.KeycloakTokenResponse;
import com.identityservice.dto.request.ChangeDefaultRoleRequest;
import com.identityservice.dto.request.LoginRequest;
import com.identityservice.dto.request.RefreshTokenRequest;
import com.identityservice.dto.request.RegisterRequest;
import com.identityservice.dto.response.AuthResponse;
import com.identityservice.dto.response.JwtTokenResponse;
import com.identityservice.dto.response.UserPrivateResponse;
import com.identityservice.entity.ExternalIdentity;
import com.identityservice.entity.User;
import com.identityservice.entity.UserRole;
import com.identityservice.enums.UserStatus;
import com.identityservice.exception.IdentityException;
import com.identityservice.exception.RoleErrorCode;
import com.identityservice.exception.UserErrorCode;
import com.identityservice.mapper.AuthMapper;
import com.identityservice.mapper.UserMapper;
import com.identityservice.repository.ExternalIdentityRepository;
import com.identityservice.repository.UserRepository;
import com.identityservice.repository.UserRoleRepository;
import com.identityservice.security.CurrentUserFacade;
import com.identityservice.service.AuthService;
import com.identityservice.service.UserService;
import com.identityservice.util.JwtClaimUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final ExternalIdentityRepository externalIdentityRepository;
    private final KeycloakAuthClient keycloakAuthClient;
    private final KeycloakAdminClient keycloakAdminClient;
    private final AuthMapper authMapper;
    private final UserMapper userMapper;
    private final CurrentUserFacade currentUserFacade;

    @Override
    public UserPrivateResponse register(RegisterRequest request) {
        UserPrivateResponse createdUser = userService.createUser(request);
        User user = userRepository.findByPublicIdAndDeletedAtIsNull(createdUser.getPublicId())
                .orElseThrow(() -> new IdentityException(UserErrorCode.USER_NOT_FOUND));
        List<UserRole> userRoles = loadUserRoles(user);
        List<String> roles = toRoleCodes(userRoles);
        String externalSubject = keycloakAdminClient.createUser(user, request.getPassword(), roles);
        createExternalIdentity(user, externalSubject);
        return userMapper.toPrivateResponse(user, toRoleCodes(userRoles), resolveDefaultRoleCode(userRoles));
    }

    @Override
    public AuthResponse login(LoginRequest request, String deviceInfo, String ipAddress) {
        KeycloakTokenResponse token = keycloakAuthClient.login(request.getEmail(), request.getPassword());
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.getEmail())
                .orElseGet(() -> findUserByExternalSubject(token.getAccessToken()));
        validateUserStatus(user);
        return buildAuthResponse(user, loadUserRoles(user), token);
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request, String deviceInfo, String ipAddress) {
        KeycloakTokenResponse token = keycloakAuthClient.refresh(request.getRefreshToken());
        User user = findUserByExternalSubject(token.getAccessToken());
        validateUserStatus(user);
        return buildAuthResponse(user, loadUserRoles(user), token);
    }

    @Override
    public void logout(RefreshTokenRequest request) {
        keycloakAuthClient.logout(request.getRefreshToken());
    }

    @Override
    public void logoutAll() {
        User user = userRepository.findByIdAndDeletedAtIsNull(currentUserFacade.getCurrentUserId())
                .orElseThrow(() -> new IdentityException(UserErrorCode.USER_NOT_FOUND));
        ExternalIdentity externalIdentity = externalIdentityRepository.findByProviderAndUser("KEYCLOAK", user)
                .orElseThrow(() -> new IdentityException(UserErrorCode.USER_NOT_FOUND));
        keycloakAdminClient.logoutUserSessions(externalIdentity.getExternalSubject());
    }

    @Override
    public UserPrivateResponse changeDefaultRole(ChangeDefaultRoleRequest request) {
        User user = userRepository.findByIdAndDeletedAtIsNull(currentUserFacade.getCurrentUserId())
                .orElseThrow(() -> new IdentityException(UserErrorCode.USER_NOT_FOUND));
        List<UserRole> userRoles = loadUserRoles(user);
        String requestedRole = request.getRole().trim().toUpperCase();
        UserRole newDefaultRole = userRoles.stream()
                .filter(userRole -> userRole.getRole().getCode().equals(requestedRole))
                .findFirst()
                .orElseThrow(() -> new IdentityException(RoleErrorCode.ROLE_NOT_ASSIGNED));

        userRoles.forEach(userRole -> userRole.setDefaultRole(false));
        newDefaultRole.setDefaultRole(true);
        userRoleRepository.saveAll(userRoles);

        List<UserRole> updatedUserRoles = loadUserRoles(user);
        return userMapper.toPrivateResponse(user, toRoleCodes(updatedUserRoles), resolveDefaultRoleCode(updatedUserRoles));
    }

    private AuthResponse buildAuthResponse(User user, List<UserRole> userRoles, KeycloakTokenResponse token) {
        List<String> roles = toRoleCodes(userRoles);
        String defaultRole = resolveDefaultRoleCode(userRoles);
        JwtTokenResponse jwtTokenResponse = authMapper.toJWTTokenResponse(
                token.getAccessToken(),
                token.getRefreshToken(),
                token.getExpiresIn()
        );
        return authMapper.toAuthResponse(userMapper.toPrivateResponse(user, roles, defaultRole), jwtTokenResponse);
    }

    private User findUserByExternalSubject(String accessToken) {
        String subject = JwtClaimUtils.stringClaim(accessToken, "sub");
        return externalIdentityRepository.findByProviderAndExternalSubject("KEYCLOAK", subject)
                .map(ExternalIdentity::getUser)
                .orElseThrow(() -> new IdentityException(UserErrorCode.USER_NOT_FOUND));
    }

    private void createExternalIdentity(User user, String externalSubject) {
        LocalDateTime now = LocalDateTime.now();
        ExternalIdentity externalIdentity = ExternalIdentity.builder()
                .provider("KEYCLOAK")
                .externalSubject(externalSubject)
                .user(user)
                .username(user.getEmail())
                .email(user.getEmail())
                .status("ACTIVE")
                .createdAt(now)
                .updatedAt(now)
                .build();
        externalIdentityRepository.save(externalIdentity);
    }

    private List<UserRole> loadUserRoles(User user) {
        return userRoleRepository.findAllByUserAndDeletedAtIsNull(user);
    }

    private List<String> toRoleCodes(List<UserRole> userRoles) {
        return userRoles.stream()
                .map(userRole -> userRole.getRole().getCode())
                .toList();
    }

    private String resolveDefaultRoleCode(List<UserRole> userRoles) {
        return userRoles.stream()
                .filter(UserRole::isDefaultRole)
                .findFirst()
                .or(() -> userRoles.stream().findFirst())
                .map(userRole -> userRole.getRole().getCode())
                .orElse(null);
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
