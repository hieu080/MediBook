package com.identityservice.dto.response;

import lombok.Builder;
import lombok.Getter;

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
