package com.sharekernel.exception;

import org.springframework.http.HttpStatus;

/**
 * Common error codes cho tất cả các module trong ứng dụng. Các module cụ thể có thể định nghĩa thêm error code riêng nếu cần.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */

public enum CommonErrorCode implements BaseErrorCode{
    VALIDATION_ERROR(
            "VALIDATION_ERROR",
            "urn:problem:validation-error",
            "Validation failed",
            HttpStatus.BAD_REQUEST,
            "Request is invalid"
    ),
    BAD_REQUEST(
            "BAD_REQUEST",
            "urn:problem:bad-request",
            "Bad request",
            HttpStatus.BAD_REQUEST,
            "Bad request"
    ),
    INVALID_FIELD_TYPE(
            "INVALID_FIELD_TYPE",
            "urn:problem:invalid-field-type",
            "Invalid field type",
            HttpStatus.BAD_REQUEST,
            "Field data type is invalid"
    ),
    UNAUTHORIZED(
            "UNAUTHORIZED",
            "urn:problem:unauthorized",
            "Unauthorized",
            HttpStatus.UNAUTHORIZED,
            "Authentication required or token is invalid"
    ),
    FORBIDDEN(
            "FORBIDDEN",
            "urn:problem:forbidden",
            "Forbidden",
            HttpStatus.FORBIDDEN,
            "You do not have permission to access this resource"
    ),
    IDEMPOTENCY_KEY_REQUIRED(
            "IDEMPOTENCY_KEY_REQUIRED",
            "urn:problem:idempotency-key-required",
            "Idempotency key required",
            HttpStatus.BAD_REQUEST,
            "Idempotency-Key header is required"
    ),
    IDEMPOTENCY_KEY_CONFLICT(
            "IDEMPOTENCY_KEY_CONFLICT",
            "urn:problem:idempotency-key-conflict",
            "Idempotency key conflict",
            HttpStatus.CONFLICT,
            "Idempotency-Key was already used with a different payload"
    ),
    RATE_LIMIT_EXCEEDED(
            "RATE_LIMIT_EXCEEDED",
            "urn:problem:rate-limit-exceeded",
            "Too many requests",
            HttpStatus.TOO_MANY_REQUESTS,
            "Rate limit exceeded"
    ),
    RESOURCE_NOT_FOUND(
            "RESOURCE_NOT_FOUND",
            "urn:problem:resource-not-found",
            "Resource not found",
            HttpStatus.NOT_FOUND,
            "Requested resource not found"
    ),
    INTERNAL_ERROR(
            "INTERNAL_ERROR",
            "urn:problem:internal-error",
            "Internal server error",
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Unexpected server error"
    );

    private final String code;
    private final String type;
    private final String title;
    private final HttpStatus httpStatus;
    private final String defaultDetail;

    CommonErrorCode(String code, String type, String title, HttpStatus httpStatus, String defaultDetail) {
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
