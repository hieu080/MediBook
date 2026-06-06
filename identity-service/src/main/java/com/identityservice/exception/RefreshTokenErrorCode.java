package com.identityservice.exception;

import com.sharekernel.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Mã lỗi nghiệp vụ liên quan đến refresh token trong identity-service.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */
public enum RefreshTokenErrorCode implements BaseErrorCode {
    REFRESH_TOKEN_INVALID(
            "REFRESH_TOKEN_INVALID",
            "urn:problem:refresh-token-invalid",
            "Refresh token invalid",
            HttpStatus.UNAUTHORIZED,
            "Refresh token không hợp lệ"
    ),
    REFRESH_TOKEN_EXPIRED(
            "REFRESH_TOKEN_EXPIRED",
            "urn:problem:refresh-token-expired",
            "Refresh token expired",
            HttpStatus.UNAUTHORIZED,
            "Refresh token đã hết hạn"
    ),
    REFRESH_TOKEN_REVOKED(
            "REFRESH_TOKEN_REVOKED",
            "urn:problem:refresh-token-revoked",
            "Refresh token revoked",
            HttpStatus.UNAUTHORIZED,
            "Refresh token đã bị thu hồi"
    );

    private final String code;
    private final String type;
    private final String title;
    private final HttpStatus httpStatus;
    private final String defaultDetail;

    RefreshTokenErrorCode(String code, String type, String title, HttpStatus httpStatus, String defaultDetail) {
        this.code = code;
        this.type = type;
        this.title = title;
        this.httpStatus = httpStatus;
        this.defaultDetail = defaultDetail;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public HttpStatus httpStatus() {
        return httpStatus;
    }

    @Override
    public String defaultDetail() {
        return defaultDetail;
    }
}
