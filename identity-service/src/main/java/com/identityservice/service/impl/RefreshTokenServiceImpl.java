package com.identityservice.service.impl;

import com.identityservice.entity.RefreshToken;
import com.identityservice.entity.User;
import com.identityservice.exception.IdentityException;
import com.identityservice.exception.RefreshTokenErrorCode;
import com.identityservice.repository.RefreshTokenRepository;
import com.identityservice.security.JwtProperties;
import com.identityservice.security.JwtService;
import com.identityservice.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

/**
 * Triển khai nghiệp vụ quản lý refresh token trong identity-service.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    @Override
    public String createRefreshToken(User user, String deviceInfo, String ipAddress) {
        String rawRefreshToken = jwtService.generateRefreshToken();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(hashToken(rawRefreshToken))
                .expiresAt(System.currentTimeMillis() + jwtProperties.getRefreshTokenExpiration())
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .build();
        refreshTokenRepository.save(refreshToken);
        return rawRefreshToken;
    }

    @Override
    public RefreshToken validateRefreshToken(String refreshToken) {
        RefreshToken storedRefreshToken = refreshTokenRepository.findByTokenHash(hashToken(refreshToken))
                .orElseThrow(() -> new IdentityException(RefreshTokenErrorCode.REFRESH_TOKEN_INVALID));

        if (storedRefreshToken.getRevokedAt() != null) {
            throw new IdentityException(RefreshTokenErrorCode.REFRESH_TOKEN_REVOKED);
        }

        if (storedRefreshToken.getExpiresAt() < System.currentTimeMillis()) {
            storedRefreshToken.setRevokedAt(System.currentTimeMillis());
            refreshTokenRepository.save(storedRefreshToken);
            throw new IdentityException(RefreshTokenErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        return storedRefreshToken;
    }

    @Override
    public void revokeRefreshToken(String refreshToken) {
        refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(hashToken(refreshToken))
                .ifPresent(token -> {
                    token.setRevokedAt(System.currentTimeMillis());
                    refreshTokenRepository.save(token);
                });
    }

    @Override
    public void revokeAllByUser(User user) {
        List<RefreshToken> activeTokens = refreshTokenRepository.findAllByUserAndRevokedAtIsNull(user);
        long revokedAt = System.currentTimeMillis();
        activeTokens.forEach(token -> token.setRevokedAt(revokedAt));
        refreshTokenRepository.saveAll(activeTokens);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Không thể hash refresh token", ex);
        }
    }
}
