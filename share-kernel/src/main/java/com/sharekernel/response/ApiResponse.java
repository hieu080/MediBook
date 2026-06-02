package com.sharekernel.response;

import java.time.Instant;

/**
 * DTO phản hồi API dùng chung cho toàn hệ thống.
 * Chứa dữ liệu trả về, thông điệp phản hồi và metadata của request.
 *
 * @param data dữ liệu chính của phản hồi
 * @param message thông điệp mô tả kết quả xử lý
 * @param meta metadata kèm theo phản hồi
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */
public record ApiResponse<T>(T data, String message, Meta meta) {

    public static <T> ApiResponse<T> success(T data, String message, String requestId) {
        return new ApiResponse<>(data, message, new Meta(requestId, Instant.now()));
    }
}
