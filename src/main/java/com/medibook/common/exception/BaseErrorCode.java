package com.medibook.common.exception;

import org.springframework.http.HttpStatus;

/**
 * BaseErrorCode là một giao diện định nghĩa các phương thức cần thiết để mô tả mã lỗi, loại lỗi, tiêu đề lỗi, trạng thái HTTP và chi tiết mặc định của lỗi.
 * Giao diện này được sử dụng để chuẩn hóa cách thức quản lý lỗi trong ứng dụng, giúp cho việc xử lý lỗi trở nên nhất quán và dễ dàng hơn.
 *
 * @author hieu080
 * @version 1.0
 * @since 2026-05-28
 */
public interface BaseErrorCode {
    String code();
    String type();
    String title();
    HttpStatus httpStatus();
    String defaultDetail();
}
