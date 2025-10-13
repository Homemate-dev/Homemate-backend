package com.zerobase.homemate.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
}
