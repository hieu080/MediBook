package com.identityservice.exception;

import com.sharekernel.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Mã lỗi nghiệp vụ liên quan đến xác thực trong identity-service.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */
public enum AuthErrorCode implements BaseErrorCode {
    INVALID_CREDENTIALS(
            "INVALID_CREDENTIALS",
            "urn:problem:invalid-credentials",
            "Invalid credentials",
            HttpStatus.UNAUTHORIZED,
            "Email hoặc mật khẩu không đúng"
    ),
    AUTHENTICATION_REQUIRED(
            "AUTHENTICATION_REQUIRED",
            "urn:problem:authentication-required",
            "Authentication required",
            HttpStatus.UNAUTHORIZED,
            "Cần đăng nhập để truy cập tài nguyên này"
    ),
    ACCESS_DENIED(
            "ACCESS_DENIED",
            "urn:problem:access-denied",
            "Access denied",
            HttpStatus.FORBIDDEN,
            "Bạn không có quyền truy cập tài nguyên này"
    );

    private final String code;
    private final String type;
    private final String title;
    private final HttpStatus httpStatus;
    private final String defaultDetail;

    AuthErrorCode(String code, String type, String title, HttpStatus httpStatus, String defaultDetail) {
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
