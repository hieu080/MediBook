package com.identityservice.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * DTO chứa thông tin của một JWK key, được sử dụng để xác thực JWT token.
 *
 * @author anhdvv
 * @since 2026-06
 * @version 1.0
 */

@Getter
@Builder
public class JwkKeyResponse {
    private String kty;
    private String kid;
    private String use;
    private String alg;
    private String n;
    private String e;
}
