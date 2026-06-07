package com.patientservice.exception;

import com.sharekernel.exception.AppException;
import com.sharekernel.exception.CommonErrorCode;
import com.sharekernel.exception.ErrorDetail;
import com.sharekernel.exception.ProblemFactory;
import com.sharekernel.web.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Handler tập trung cho toàn bộ exception trong identity-service.
 * Lớp này chuẩn hóa phản hồi lỗi theo ProblemDetail và dùng chung
 * các cấu phần từ share-kernel.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ProblemFactory problemFactory;

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ProblemDetail> handleAppException(
            AppException exception,
            HttpServletRequest request
    ) {
        ProblemDetail problemDetail = problemFactory.create(
                exception.getErrorCode(),
                exception.getMessage(),
                request.getRequestURI(),
                extractRequestId(request),
                exception.getErrors()
        );

        return ResponseEntity.status(exception.getErrorCode().httpStatus()).body(problemDetail);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<ErrorDetail> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toErrorDetail)
                .toList();

        ProblemDetail problemDetail = problemFactory.create(
                CommonErrorCode.VALIDATION_ERROR,
                CommonErrorCode.VALIDATION_ERROR.defaultDetail(),
                request.getRequestURI(),
                extractRequestId(request),
                errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolationException(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        List<ErrorDetail> errors = exception.getConstraintViolations()
                .stream()
                .map(violation -> new ErrorDetail(
                        violation.getPropertyPath().toString(),
                        CommonErrorCode.VALIDATION_ERROR.code(),
                        violation.getMessage()
                ))
                .toList();

        ProblemDetail problemDetail = problemFactory.create(
                CommonErrorCode.VALIDATION_ERROR,
                CommonErrorCode.VALIDATION_ERROR.defaultDetail(),
                request.getRequestURI(),
                extractRequestId(request),
                errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        ProblemDetail problemDetail = problemFactory.create(
                CommonErrorCode.INVALID_FIELD_TYPE,
                CommonErrorCode.INVALID_FIELD_TYPE.defaultDetail(),
                request.getRequestURI(),
                extractRequestId(request),
                null
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleException(
            Exception exception,
            HttpServletRequest request
    ) {
        ProblemDetail problemDetail = problemFactory.create(
                CommonErrorCode.INTERNAL_ERROR,
                CommonErrorCode.INTERNAL_ERROR.defaultDetail(),
                request.getRequestURI(),
                extractRequestId(request),
                null
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }

    private ErrorDetail toErrorDetail(FieldError fieldError) {
        return new ErrorDetail(
                fieldError.getField(),
                fieldError.getCode(),
                fieldError.getDefaultMessage()
        );
    }

    private String extractRequestId(HttpServletRequest request) {
        Object requestId = request.getAttribute(RequestIdFilter.REQUEST_ID_ATTR);
        return requestId == null ? null : requestId.toString();
    }
}
