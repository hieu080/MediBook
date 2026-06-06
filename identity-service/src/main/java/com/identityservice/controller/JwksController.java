package com.identityservice.controller;

import com.identityservice.dto.response.JwkKeyResponse;
import com.identityservice.dto.response.JwksResponse;
import com.identityservice.security.JwtKeyProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth/.well-known")
@RequiredArgsConstructor
public class JwksController {

    private final JwtKeyProvider jwtKeyProvider;

    @GetMapping("/jwks.json")
    public JwksResponse getJwks() {
        JwkKeyResponse key = JwkKeyResponse.builder()
                .kty("RSA")
                .kid(jwtKeyProvider.getKeyId())
                .use("sig")
                .alg("RS256")
                .n(jwtKeyProvider.getModulus())
                .e(jwtKeyProvider.getPublicExponent())
                .build();
        return JwksResponse.builder()
                .keys(List.of(key))
                .build();
    }
}
