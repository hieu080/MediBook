package com.sharekernel.exception;

import org.springframework.http.HttpStatus;

/**
 * Giao diện BaseErrorCode định nghĩa cấu trúc cho các mã lỗi trong hệ thống, bao gồm mã lỗi, loại lỗi, tiêu đề, trạng thái HTTP và chi tiết mặc định.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */

public interface BaseErrorCode {
    String code();

    String type();

    String title();

    HttpStatus httpStatus();

    String defaultDetail();
}
