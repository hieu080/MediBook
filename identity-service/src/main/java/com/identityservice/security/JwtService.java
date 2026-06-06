package com.identityservice.security;

import com.identityservice.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service thao tác với JSON Web Token, bao gồm tạo, đọc và kiểm tra token.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;
    private final JwtClaimsFactory jwtClaimsFactory;
    private final JwtKeyProvider jwtKeyProvider;

    public String generateAccessToken(User user, List<String> roles) {
        long now = System.currentTimeMillis();
        Map<String, Object> claims = jwtClaimsFactory.createAccessTokenClaims(user, roles);
        return Jwts.builder()
                .header()
                .keyId(jwtKeyProvider.getKeyId())
                .and()
                .claims(claims)
                .subject(user.getEmail())
                .issuer(jwtProperties.getIssuer())
                .issuedAt(new Date(now))
                .expiration(new Date(now + jwtProperties.getAccessTokenExpiration()))
                .signWith(jwtKeyProvider.getPrivateKey(), Jwts.SIG.RS256)
                .compact();
    }

    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(jwtKeyProvider.getPublicKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractSubject(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            Date expiration = extractAllClaims(token).getExpiration();
            return expiration != null && expiration.after(new Date());
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public long getAccessTokenExpiration() {
        return jwtProperties.getAccessTokenExpiration() / 1000L;
    }

}
