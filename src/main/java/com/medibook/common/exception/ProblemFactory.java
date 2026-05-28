package com.medibook.common.exception;

import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;

/**
 * ProblemFactory là một thành phần Spring được sử dụng để tạo ra các đối tượng ProblemDetail dựa trên mã lỗi và thông tin chi tiết.
 * Lớp này giúp chuẩn hóa cách thức tạo ra các phản hồi lỗi theo định dạng Problem Details for HTTP APIs (RFC 7807).
 *
 * @author hieu080
 * @version 1.0
 * @since 2026-05-28
 */

@Component
public class ProblemFactory {
    public ProblemDetail createProblemDetail(
            BaseErrorCode errorCode,
            String detail,
            String instance,
            String requestId,
            List<ErrorDetail> errors
    ){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(errorCode.httpStatus(), detail);
        problemDetail.setType(URI.create(errorCode.type()));
        problemDetail.setTitle(errorCode.title());

        if (instance != null && !instance.isBlank()) {
            problemDetail.setInstance(URI.create(instance));
        }

        problemDetail.setProperty("code", errorCode.code());

        if (requestId != null && !requestId.isBlank()) {
            problemDetail.setProperty("requestId", requestId);
        }

        if (errors != null && !errors.isEmpty()) {
            problemDetail.setProperty("errors", errors);
        }

        return problemDetail;
    }
}
