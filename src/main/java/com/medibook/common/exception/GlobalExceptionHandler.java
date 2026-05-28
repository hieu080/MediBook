package com.medibook.common.exception;

import com.medibook.common.web.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final ProblemFactory problemFactory;

    public GlobalExceptionHandler(ProblemFactory problemFactory) {
        this.problemFactory = problemFactory;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ){
        List<ErrorDetail> errors = ex.getBindingResult().getFieldErrors().stream().map(this::toErrorDetail).toList();

        ProblemDetail problemDetail = problemFactory.createProblemDetail(
                CommonErrorCode.VALIDATION_ERROR,
                "Request body is invalid",
                request.getRequestURI(),
                getRequestId(request),
                errors
        );

        return ResponseEntity
                .status(CommonErrorCode.VALIDATION_ERROR.httpStatus())
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problemDetail);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        List<ErrorDetail> errors = ex.getConstraintViolations()
                .stream()
                .map(v -> new ErrorDetail(v.getPropertyPath().toString(), "CONSTRAINT_VIOLATION", v.getMessage()))
                .toList();

        ProblemDetail problemDetail = problemFactory.createProblemDetail(
                CommonErrorCode.VALIDATION_ERROR,
                "Request is invalid",
                request.getRequestURI(),
                getRequestId(request),
                errors
        );

        return ResponseEntity
                .status(CommonErrorCode.VALIDATION_ERROR.httpStatus())
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problemDetail);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        Optional<ErrorDetail> reflectiveTypeError = parseInvalidFieldTypeFromThrowableChain(ex);
        if (reflectiveTypeError.isPresent()) {
            ProblemDetail problemDetail = problemFactory.createProblemDetail(
                    CommonErrorCode.INVALID_FIELD_TYPE,
                    "Request body contains invalid data type",
                    request.getRequestURI(),
                    getRequestId(request),
                    List.of(reflectiveTypeError.get())
            );

            return ResponseEntity
                    .status(CommonErrorCode.INVALID_FIELD_TYPE.httpStatus())
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .body(problemDetail);
        }

        Optional<ErrorDetail> parsedTypeError = parseInvalidFieldType(ex);
        if (parsedTypeError.isPresent()) {
            ProblemDetail problemDetail = problemFactory.createProblemDetail(
                    CommonErrorCode.INVALID_FIELD_TYPE,
                    "Request body contains invalid data type",
                    request.getRequestURI(),
                    getRequestId(request),
                    List.of(parsedTypeError.get())
            );

            return ResponseEntity
                    .status(CommonErrorCode.INVALID_FIELD_TYPE.httpStatus())
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .body(problemDetail);
        }

        ProblemDetail problemDetail = problemFactory.createProblemDetail(
                CommonErrorCode.BAD_REQUEST,
                "Malformed JSON request body",
                request.getRequestURI(),
                getRequestId(request),
                List.of()
        );

        return ResponseEntity
                .status(CommonErrorCode.BAD_REQUEST.httpStatus())
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problemDetail);
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ProblemDetail> handleAppException(AppException ex, HttpServletRequest request) {
        BaseErrorCode errorCode = ex.getErrorCode();
        ProblemDetail problemDetail = problemFactory.createProblemDetail(
                errorCode,
                ex.getMessage(),
                request.getRequestURI(),
                getRequestId(request),
                ex.getErrors()
        );

        return ResponseEntity
                .status(errorCode.httpStatus())
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problemDetail);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception ex, HttpServletRequest request) {
        ProblemDetail problemDetail = problemFactory.createProblemDetail(
                CommonErrorCode.INTERNAL_ERROR,
                CommonErrorCode.INTERNAL_ERROR.defaultDetail(),
                request.getRequestURI(),
                getRequestId(request),
                List.of()
        );

        return ResponseEntity
                .status(CommonErrorCode.INTERNAL_ERROR.httpStatus())
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problemDetail);
    }

    private ErrorDetail toErrorDetail(FieldError fieldError) {
        return new ErrorDetail(
                fieldError.getField(),
                fieldError.getCode() == null ? "VALIDATION_ERROR" : fieldError.getCode(),
                fieldError.getDefaultMessage() == null ? "Invalid field" : fieldError.getDefaultMessage()
        );
    }

    private String getRequestId(HttpServletRequest request) {
        Object requestId = request.getAttribute(RequestIdFilter.REQUEST_ID_ATTR);
        return requestId == null ? null : requestId.toString();
    }

    private String toUserType(Class<?> targetType) {
        if (targetType == null) {
            return "valid value";
        }

        if (targetType == String.class) {
            return "string";
        }
        if (targetType == Boolean.class || targetType == boolean.class) {
            return "boolean";
        }
        if (targetType == Byte.class || targetType == byte.class ||
                targetType == Short.class || targetType == short.class ||
                targetType == Integer.class || targetType == int.class ||
                targetType == Long.class || targetType == long.class ||
                targetType == Float.class || targetType == float.class ||
                targetType == Double.class || targetType == double.class ||
                "BigDecimal".equals(targetType.getSimpleName()) ||
                "BigInteger".equals(targetType.getSimpleName())) {
            return "number";
        }
        if ("LocalDate".equals(targetType.getSimpleName())) {
            return "date (yyyy-MM-dd)";
        }
        if ("LocalDateTime".equals(targetType.getSimpleName())) {
            return "datetime (yyyy-MM-dd'T'HH:mm:ss)";
        }

        return targetType.getSimpleName();
    }

    private Optional<ErrorDetail> parseInvalidFieldType(HttpMessageNotReadableException ex) {
        String message = ex.getMessage();
        if (message == null || !message.contains("Cannot deserialize value of type")) {
            return Optional.empty();
        }

        String field = extractFieldFromMessage(message);
        String expectedType = extractTypeFromMessage(message).map(this::toUserType).orElse("valid value");

        return Optional.of(
                new ErrorDetail(field, CommonErrorCode.INVALID_FIELD_TYPE.code(), "Field '" + field + "': Must be " + expectedType)
        );
    }

    private Optional<ErrorDetail> parseInvalidFieldTypeFromCause(Throwable cause) {
        if (cause == null) {
            return Optional.empty();
        }
        try {
            String field = "unknown";
            String expectedType = "valid value";

            try {
                Method getPath = cause.getClass().getMethod("getPath");
                Object pathObj = getPath.invoke(cause);
                if (pathObj instanceof List<?> pathList && !pathList.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (Object ref : pathList) {
                        if (ref == null) {
                            continue;
                        }
                        String part = null;
                        try {
                            Method getFieldName = ref.getClass().getMethod("getFieldName");
                            Object nameObj = getFieldName.invoke(ref);
                            if (nameObj != null) {
                                part = nameObj.toString();
                            }
                        } catch (Exception ignored) {
                        }
                        if (part == null) {
                            try {
                                Method getIndex = ref.getClass().getMethod("getIndex");
                                Object idxObj = getIndex.invoke(ref);
                                if (idxObj != null && Integer.parseInt(idxObj.toString()) >= 0) {
                                    part = "[" + idxObj + "]";
                                }
                            } catch (Exception ignored) {
                            }
                        }
                        if (part != null) {
                            if (!sb.isEmpty()) {
                                sb.append(".");
                            }
                            sb.append(part);
                        }
                    }
                    if (!sb.isEmpty()) {
                        field = sb.toString();
                    }
                }
            } catch (Exception ignored) {
            }

            try {
                Method getTargetType = cause.getClass().getMethod("getTargetType");
                Object targetTypeObj = getTargetType.invoke(cause);
                if (targetTypeObj instanceof Class<?> cls) {
                    expectedType = toUserType(cls);
                } else if (targetTypeObj != null) {
                    expectedType = toUserType(targetTypeObj.toString());
                }
            } catch (Exception ignored) {
            }

            String message = cause.getMessage();
            if (message != null && message.contains("Cannot deserialize value of type")) {
                if ("unknown".equals(field)) {
                    field = extractFieldFromMessage(message);
                }
                expectedType = extractTypeFromMessage(message).map(this::toUserType).orElse(expectedType);
            }

            if (!cause.getClass().getSimpleName().contains("InvalidFormatException")
                    && (message == null || !message.contains("Cannot deserialize value of type"))) {
                return Optional.empty();
            }

            return Optional.of(
                    new ErrorDetail(field, CommonErrorCode.INVALID_FIELD_TYPE.code(), "Field '" + field + "': Must be " + expectedType)
            );
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private Optional<ErrorDetail> parseInvalidFieldTypeFromThrowableChain(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            Optional<ErrorDetail> parsed = parseInvalidFieldTypeFromCause(current);
            if (parsed.isPresent() && !"unknown".equals(parsed.get().field())) {
                return parsed;
            }
            current = current.getCause();
        }

        current = throwable;
        while (current != null) {
            Optional<ErrorDetail> parsed = parseInvalidFieldTypeFromCause(current);
            if (parsed.isPresent()) {
                return parsed;
            }
            current = current.getCause();
        }

        return Optional.empty();
    }

    private String extractFieldFromMessage(String message) {
        Pattern fieldPattern = Pattern.compile("\\[\\\"([^\\\"]+)\\\"\\]");
        Matcher matcher = fieldPattern.matcher(message);
        String lastField = "unknown";
        while (matcher.find()) {
            lastField = matcher.group(1);
        }
        return lastField;
    }

    private Optional<String> extractTypeFromMessage(String message) {
        Pattern typePattern = Pattern.compile("Cannot deserialize value of type `([^`]+)`");
        Matcher matcher = typePattern.matcher(message);
        if (matcher.find()) {
            return Optional.of(matcher.group(1));
        }
        return Optional.empty();
    }

    private String toUserType(String typeName) {
        if (typeName == null || typeName.isBlank()) {
            return "valid value";
        }

        if (typeName.contains("BigDecimal")
                || typeName.contains("BigInteger")
                || typeName.endsWith("Integer")
                || typeName.endsWith("Long")
                || typeName.endsWith("Double")
                || typeName.endsWith("Float")
                || typeName.endsWith("Short")
                || typeName.endsWith("Byte")
                || "int".equals(typeName)
                || "long".equals(typeName)
                || "double".equals(typeName)
                || "float".equals(typeName)
                || "short".equals(typeName)
                || "byte".equals(typeName)) {
            return "number";
        }
        if (typeName.contains("Boolean") || "boolean".equals(typeName)) {
            return "boolean";
        }
        if (typeName.contains("String")) {
            return "string";
        }
        if (typeName.contains("LocalDateTime")) {
            return "datetime (yyyy-MM-dd'T'HH:mm:ss)";
        }
        if (typeName.contains("LocalDate")) {
            return "date (yyyy-MM-dd)";
        }

        int idx = typeName.lastIndexOf('.');
        return idx >= 0 ? typeName.substring(idx + 1) : typeName;
    }
}
