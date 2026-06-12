package com.identityservice.exception;

import com.sharekernel.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Mã lỗi nghiệp vụ liên quan đến vai trò người dùng trong identity-service.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */
public enum RoleErrorCode implements BaseErrorCode {
    ROLE_NOT_FOUND(
            "ROLE_NOT_FOUND",
            "urn:problem:role-not-found",
            "Role not found",
            HttpStatus.NOT_FOUND,
            "Không tìm thấy vai trò với ID hoặc tên đã cho"
    ),
    ROLE_ALREADY_ASSIGNED(
            "ROLE_ALREADY_ASSIGNED",
            "urn:problem:role-already-assigned",
            "Role already assigned",
            HttpStatus.CONFLICT,
            "Vai trò đã được gán cho người dùng này"
    ),
    ROLE_NOT_ASSIGNED(
            "ROLE_NOT_ASSIGNED",
            "urn:problem:role-not-assigned",
            "Role not assigned",
            HttpStatus.FORBIDDEN,
            "Người dùng hiện tại chưa được gán vai trò này"
    );

    private final String code;
    private final String type;
    private final String title;
    private final HttpStatus httpStatus;
    private final String defaultDetail;

    RoleErrorCode(String code, String type, String title, HttpStatus httpStatus, String defaultDetail) {
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
