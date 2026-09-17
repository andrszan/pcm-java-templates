package com.example.app.common;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局错误处理：错误体统一包装为 ApiResponse，信息只包含经过审查的公开内容，
 * 不回显原始输入、SQL、异常对象或内部细节；原始异常只进日志。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> notFound(NoResourceFoundException ignored) {
        return envelope(HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HandlerMethodValidationException.class})
    public ResponseEntity<ApiResponse<Object>> validation(Exception exception) {
        List<FieldErrorView> errors = switch (exception) {
            case MethodArgumentNotValidException e -> e.getBindingResult().getFieldErrors().stream()
                    .map(error -> new FieldErrorView(error.getField(), error.getDefaultMessage()))
                    .toList();
            case HandlerMethodValidationException e -> e.getParameterValidationResults().stream()
                    .flatMap(result -> result.getResolvableErrors().stream()
                            .map(error -> new FieldErrorView(
                                    result.getMethodParameter().getParameterName(),
                                    error.getDefaultMessage())))
                    .toList();
            default -> List.of();
        };
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.of(422, "Request validation failed", new ValidationData(errors)));
    }

    @ExceptionHandler({
            HttpRequestMethodNotSupportedException.class,
            HttpMediaTypeNotSupportedException.class,
            HttpMediaTypeNotAcceptableException.class,
            HttpMessageNotReadableException.class,
            ServletRequestBindingException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiResponse<Void>> clientError(Exception exception) {
        HttpStatus status = switch (exception) {
            case HttpRequestMethodNotSupportedException ignored -> HttpStatus.METHOD_NOT_ALLOWED;
            case HttpMediaTypeNotSupportedException ignored -> HttpStatus.UNSUPPORTED_MEDIA_TYPE;
            case HttpMediaTypeNotAcceptableException ignored -> HttpStatus.NOT_ACCEPTABLE;
            default -> HttpStatus.BAD_REQUEST;
        };
        return envelope(status);
    }

    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ApiResponse<Void>> responseStatus(ErrorResponseException exception) {
        return envelope(exception.getStatusCode());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> unhandled(Exception exception) {
        log.error("未处理异常 request_id={}", org.slf4j.MDC.get("requestId"), exception);
        return envelope(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiResponse<Void>> envelope(HttpStatusCode status) {
        HttpStatus resolved = HttpStatus.resolve(status.value());
        String message = resolved == null ? "Request Failed" : resolved.getReasonPhrase();
        return ResponseEntity.status(status)
                .body(ApiResponse.of(status.value(), message, null));
    }

    private record FieldErrorView(String field, String message) {
    }

    private record ValidationData(List<FieldErrorView> errors) {
    }

}
