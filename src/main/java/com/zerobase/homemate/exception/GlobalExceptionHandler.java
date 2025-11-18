package com.zerobase.homemate.exception;

import jakarta.servlet.ServletException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 커스텀 예외 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        log.error("CustomException 발생: {}", e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.of(
            e.getErrorCode(),
            e.getMessage()
        );

        return ResponseEntity.status(e.getErrorCode().getHttpStatus()).body(errorResponse);
    }

    // 유효성 검증 예외 처리 (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        log.error("유효성 검증 실패: {}", e.getMessage());

        List<ErrorResponse.ValidationError> details =
            e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> ErrorResponse.ValidationError.builder()
                    .field(fieldError.getField())
                    .message(fieldError.getDefaultMessage())
                    .build())
                    .toList();

        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.VALIDATION_ERROR, details);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // Redis 연결 실패
    @ExceptionHandler(RedisConnectionFailureException.class)
    public ResponseEntity<ErrorResponse> handleRedisException(RedisConnectionFailureException e) {
        log.error("Redis 연결 실패: {}", e.getMessage(), e);

        return ResponseEntity.status(ErrorCode.REFRESH_STORE_UNAVAILABLE.getHttpStatus())
            .body(ErrorResponse.of(ErrorCode.REFRESH_STORE_UNAVAILABLE));
    }

    // 요청 본문(JSON) 파싱 실패
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("본문 파싱 실패: {}", e.getMessage(), e);

        return ResponseEntity.status(ErrorCode.INVALID_REQUEST_BODY.getHttpStatus())
            .body(ErrorResponse.of(ErrorCode.INVALID_REQUEST_BODY));
    }

    // 허용되지 않은 HTTP 메서드 요청
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("허용되지 않은 HTTP 메서드 요청: {}", e.getMessage(), e);

        return ResponseEntity.status(ErrorCode.METHOD_NOT_ALLOWED.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED));
    }

    // 존재하지 않는 URI 엔드포인트 요청
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ErrorResponse> handleNoURIFoundException(ServletException e) {
        log.error("존재하지 않는 URI 요청: {}", e.getMessage(), e);

        return ResponseEntity.status(ErrorCode.URI_NOT_FOUND.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.URI_NOT_FOUND));
    }

    // 기타 오류 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("서버 내부 오류: {}", e.getMessage(), e);

        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}
