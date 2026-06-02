package com.sharekernel.exception;

import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;

/**
 * Factory để tạo đối tượng ProblemDetail từ BaseErrorCode và các thông tin chi tiết khác.
 *
 * @author hieu080
 * @since 2024-06
 * @version 1.0
 */

@Component
public class ProblemFactory {
    public ProblemDetail create(
            BaseErrorCode errorCode,
            String detail,
            String instance,
            String requestId,
            List<ErrorDetail> errors
    ) {
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
