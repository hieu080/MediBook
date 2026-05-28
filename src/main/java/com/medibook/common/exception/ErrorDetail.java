package com.medibook.common.exception;

/**
 * ErrorDetail là một lớp record trong Java, được sử dụng để lưu trữ thông tin chi tiết về lỗi xảy ra trong ứng dụng. Lớp này bao gồm ba trường: field (trường dữ liệu liên quan đến lỗi), code (mã lỗi) và message (thông điệp mô tả lỗi). Lớp này giúp cho việc truyền tải thông tin lỗi trở nên rõ ràng và dễ hiểu hơn, đặc biệt khi trả về phản hồi lỗi từ API.
 *
 * @param field
 * @param code
 * @param message
 *
 * @author hieu080
 * @version 1.0
 * @since 2026-05-28
 */
public record ErrorDetail (String field, String code, String message) {
}
