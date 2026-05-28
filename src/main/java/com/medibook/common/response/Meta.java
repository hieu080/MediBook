package com.medibook.common.response;

import java.time.Instant;

/**
 * Meta là một lớp record trong Java, được sử dụng để lưu trữ thông tin meta liên quan đến phản hồi API. Lớp này bao gồm hai trường: requestId (mã định danh duy nhất cho mỗi yêu cầu) và timestamp (thời điểm mà phản hồi được tạo ra). Meta giúp cung cấp thông tin bổ sung về phản hồi, giúp cho việc theo dõi và gỡ lỗi trở nên dễ dàng hơn, đặc biệt khi xử lý các yêu cầu phức tạp hoặc khi cần phân tích hiệu suất của API.
 * @param requestId
 * @param timestamp
 *
 * @author hieu080
 * @version 1.0
 * @since 2026-05-28
 */
public record Meta(String requestId, Instant timestamp) {
}
