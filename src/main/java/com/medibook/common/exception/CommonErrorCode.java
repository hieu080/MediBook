package com.medibook.common.exception;

import org.springframework.http.HttpStatus;

/**
 * CommonErrorCode là một enum chứa các mã lỗi phổ biến được sử dụng trong ứng dụng.
 * Mỗi mã lỗi bao gồm một mã định danh, loại lỗi, tiêu đề, trạng thái HTTP tương ứng và chi tiết mặc định.
 * Enum này giúp chuẩn hóa cách xử lý lỗi và cung cấp thông tin chi tiết về lỗi khi trả về phản hồi cho client.
 *
 * @author hieu080
 * @version 1.0
 * @since 2026-05-28
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
