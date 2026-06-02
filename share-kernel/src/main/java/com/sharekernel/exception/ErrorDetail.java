package com.sharekernel.exception;

/**
 * Chi tiết lỗi dùng trong phản hồi lỗi chuẩn của hệ thống.
 *
 * @param field tên trường dữ liệu gây lỗi
 * @param code mã lỗi chi tiết
 * @param message thông điệp mô tả lỗi
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */
public record ErrorDetail(String field, String code, String message) {
}
