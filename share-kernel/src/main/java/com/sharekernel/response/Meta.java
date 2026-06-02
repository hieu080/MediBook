package com.sharekernel.response;

import java.time.Instant;

/**
 * Metadata dùng chung cho phản hồi API.
 * Chứa mã định danh request và thời điểm phản hồi được tạo.
 *
 * @param requestId mã định danh duy nhất của request
 * @param timestamp thời điểm phản hồi được sinh ra
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */
public record Meta(String requestId, Instant timestamp) {
}
