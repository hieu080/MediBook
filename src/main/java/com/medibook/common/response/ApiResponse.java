package com.medibook.common.response;

import java.time.Instant;

/**
 * ApiResponse là một lớp record trong Java, được sử dụng để đại diện cho cấu trúc phản hồi của API.
 * Lớp này bao gồm ba trường: data (dữ liệu trả về từ API), message (thông điệp mô tả phản hồi) và meta
 * (thông tin meta liên quan đến phản hồi). ApiResponse giúp chuẩn hóa cách thức trả về dữ liệu từ API,
 * cung cấp thông tin rõ ràng và dễ hiểu cho client khi nhận được phản hồi từ server.
 *
 * @param data
 * @param message
 * @param meta
 *
 * @author hieu080
 * @version 1.0
 * @since 2026-05-28
 */

public record ApiResponse<T>(T data, String message, Meta meta) {
    public static <T> ApiResponse<T> success(T data, String message, String requestId) {
        return new ApiResponse<>(data, message, new Meta(requestId, Instant.now()));
    }
}
