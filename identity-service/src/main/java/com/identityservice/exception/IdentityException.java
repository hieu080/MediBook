package com.identityservice.exception;

import com.sharekernel.exception.AppException;
import com.sharekernel.exception.BaseErrorCode;
import com.sharekernel.exception.ErrorDetail;

import java.util.List;

/**
 * Ngoại lệ nghiệp vụ dùng chung cho identity-service.
 * Lớp này đại diện cho các lỗi thuộc miền định danh và xác thực, sử dụng
 * {@link BaseErrorCode} để chuẩn hóa mã lỗi và trạng thái phản hồi.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */
public class IdentityException extends AppException {

    public IdentityException(BaseErrorCode errorCode) {
        super(errorCode);
    }

    public IdentityException(BaseErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }

    public IdentityException(BaseErrorCode errorCode, String detail, List<ErrorDetail> errors) {
        super(errorCode, detail, errors);
    }
}
