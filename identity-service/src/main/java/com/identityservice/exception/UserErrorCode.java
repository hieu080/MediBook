package com.identityservice.exception;

import com.sharekernel.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Mã lỗi nghiệp vụ liên quan đến người dùng trong identity-service.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */
public enum UserErrorCode implements BaseErrorCode {
    USER_ALREADY_EXISTS(
            "USER_ALREADY_EXISTS",
            "urn:problem:user-already-exists",
            "User already exists",
            HttpStatus.CONFLICT,
            "Người dùng đã tồn tại"
    ),
    PASSWORD_CONFIRMATION_MISMATCH(
            "PASSWORD_CONFIRMATION_MISMATCH",
            "urn:problem:password-confirmation-mismatch",
            "Password confirmation mismatch",
            HttpStatus.BAD_REQUEST,
            "Mật khẩu nhập lại không khớp"
    ),
    USER_NOT_FOUND(
            "USER_NOT_FOUND",
            "urn:problem:user-not-found",
            "User not found",
            HttpStatus.NOT_FOUND,
            "Không tìm thấy người dùng"
    ),
    USER_INACTIVE(
            "USER_INACTIVE",
            "urn:problem:user-inactive",
            "User inactive",
            HttpStatus.FORBIDDEN,
            "Người dùng chưa được kích hoạt hoặc đã bị vô hiệu hóa"
    ),
    USER_SUSPENDED(
            "USER_SUSPENDED",
            "urn:problem:user-suspended",
            "User suspended",
            HttpStatus.FORBIDDEN,
            "Tài khoản người dùng đã bị tạm ngưng hoạt động"
    );

    private final String code;
    private final String type;
    private final String title;
    private final HttpStatus httpStatus;
    private final String defaultDetail;

    UserErrorCode(String code, String type, String title, HttpStatus httpStatus, String defaultDetail) {
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
