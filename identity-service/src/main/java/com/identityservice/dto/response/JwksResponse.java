package com.identityservice.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class JwksResponse {
    private List<JwkKeyResponse> keys;
}
