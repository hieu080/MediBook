package com.sharekernel.web;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Utility hôc trợ truy cập thông tin context của request hiện tại.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */
public final class RequestContextUtils {

    private RequestContextUtils() {
    }

    public static String getRequestId(HttpServletRequest request) {
        return (String) request.getAttribute(RequestIdFilter.REQUEST_ID_ATTR);
    }
}
