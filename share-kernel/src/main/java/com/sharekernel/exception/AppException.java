package com.sharekernel.exception;

import java.util.List;

/**
 * Lớp ngoại lệ tùy chỉnh cho ứng dụng, chứa mã lỗi và chi tiết lỗi.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */

public class AppException extends RuntimeException {
    private final BaseErrorCode errorCode;
    private final List<ErrorDetail> errors;

    public AppException(BaseErrorCode errorCode) {
        this(errorCode, errorCode.defaultDetail(), List.of());
    }

    public AppException(BaseErrorCode errorCode, String detail) {
        this(errorCode, detail, List.of());
    }

    public AppException(BaseErrorCode errorCode, String detail, List<ErrorDetail> errors) {
        super(detail);
        this.errorCode = errorCode;
        this.errors = errors == null ? List.of() : errors;
    }

    public BaseErrorCode getErrorCode() {
        return errorCode;
    }

    public List<ErrorDetail> getErrors() {
        return errors;
    }
}
