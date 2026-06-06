package com.identityservice.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * DTO phản hồi chứa thông tin về các khóa công khai (JWKs) được sử dụng để xác thực JWT.
 *
 * @author anhdvv
 * @since 2026-06
 * @version 1.0
 */

@Getter
@Builder
public class JwksResponse {
    private List<JwkKeyResponse> keys;
}
