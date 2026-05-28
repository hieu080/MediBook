package com.medibook.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        long startNanos = System.nanoTime();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long latencyMs = (System.nanoTime() - startNanos) / 1_000_000;
            String requestId = resolveRequestId(request, response);
            String endpoint = request.getMethod() + " " + request.getRequestURI();
            int status = response.getStatus();

            log.info(
                "requestId={} endpoint=\"{}\" status={} latencyMs={}",
                requestId,
                endpoint,
                status,
                latencyMs
            );
        }
    }

    private String resolveRequestId(HttpServletRequest request, HttpServletResponse response) {
        Object requestId = request.getAttribute(RequestIdFilter.REQUEST_ID_ATTR);
        if (requestId != null) {
            return requestId.toString();
        }
        String fromResponse = response.getHeader(RequestIdFilter.REQUEST_ID_HEADER);
        return fromResponse == null || fromResponse.isBlank() ? "n/a" : fromResponse;
    }
}
