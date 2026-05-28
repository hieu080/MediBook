package com.medibook.common.exception;

import java.util.List;

/**
 * AppException là một lớp ngoại lệ tùy chỉnh mở rộng từ RuntimeException,
 * được sử dụng để đại diện cho các lỗi xảy ra trong ứng dụng.
 * Lớp này chứa một mã lỗi (errorCode) và một danh sách các chi tiết lỗi (errors)
 * để cung cấp thông tin chi tiết về lỗi khi nó xảy ra. AppException
 * giúp chuẩn hóa cách thức xử lý lỗi trong ứng dụng và cung cấp thông tin
 * chi tiết về lỗi khi trả về phản hồi cho client.
 *
 * @author hieu080
 * @version 1.0
 * @since 2026-05-28
 */

public class AppException extends RuntimeException {
    private final BaseErrorCode errorCode;
    private final List<ErrorDetail> errors;

    public AppException(BaseErrorCode errorCode){
        this(errorCode, errorCode.defaultDetail(), List.of());
    }

    public AppException(BaseErrorCode errorCode, String detail){
        this(errorCode, detail, List.of());
    }

    public AppException(BaseErrorCode errorCode, String detail, List<ErrorDetail> errors) {
        super(detail);
        this.errorCode = errorCode;
        this.errors = errors;
    }

    public BaseErrorCode getErrorCode() {
        return errorCode;
    }

    public List<ErrorDetail> getErrors() {
        return errors;
    }
}
