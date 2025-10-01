package com.zerobase.homemate.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    
    // 400 Bad Request
    VALIDATION_ERROR("VALIDATION_ERROR", "입력값이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    INVALID_DATE_FORMAT("INVALID_DATE_FORMAT", "잘못된 날짜 형식입니다.", HttpStatus.BAD_REQUEST),
    INVALID_DATE_RANGE("INVALID_DATE_RANGE", "종료일은 시작일보다 이후여야 합니다.", HttpStatus.BAD_REQUEST),
    INVALID_REPEAT_INTERVAL("INVALID_REPEAT_INTERVAL", "반복 주기는 1 이상이어야 합니다.", HttpStatus.BAD_REQUEST),
    TOO_MANY_INSTANCES("TOO_MANY_INSTANCES", "생성할 인스턴스가 너무 많습니다. (최대 1000개)", HttpStatus.BAD_REQUEST),
    INVALID_NOTIFICATION_TIME("INVALID_NOTIFICATION_TIME", "알림 시간 형식이 올바르지 않습니다. (HH:mm)", HttpStatus.BAD_REQUEST),
    CHORE_ALREADY_DELETED("CHORE_ALREADY_DELETED", "이미 삭제되었거나 취소한 집안일 입니다.", HttpStatus.BAD_REQUEST),
    
    // 401 Unauthorized
    UNAUTHORIZED("UNAUTHORIZED", "인증된 토큰 값이 아닙니다.", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("TOKEN_EXPIRED", "토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    TOKEN_NOT_FOUND("TOKEN_NOT_FOUND", "토큰을 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED),
    
    // 403 Forbidden
    FORBIDDEN("FORBIDDEN", "권한이 일치하지 않습니다.", HttpStatus.FORBIDDEN),

    // 404 Not Found
    NOTIFICATION_NOT_FOUND("NOTIFICATION_NOT_FOUND", "해당 알림을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    CHORE_NOT_FOUND("CHORE_NOT_FOUND", "해당 집안일을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CHORE_INSTANCE_NOT_FOUND("CHORE_INSTANCE_NOT_FOUND", "해당 날짜의 집안일을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    
    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
    
    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
