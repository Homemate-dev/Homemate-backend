package com.zerobase.homemate.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ErrorResponse {
    
    private final int httpStatus;
    private final boolean success = false;
    private final ErrorDetail error;
    private final String timestamp;

    @Getter
    @Builder
    public static class ErrorDetail {
        private final String code;
        private final String message;
        private final List<ValidationError> details;
    }
    
    @Getter
    @Builder
    public static class ValidationError {
        private final String field;
        private final String message;
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return ErrorResponse.builder()
            .httpStatus(errorCode.getHttpStatus().value())
            .error(ErrorDetail.builder()
                .code(errorCode.getCode())
                .message(message)
                .build())
            .timestamp(LocalDateTime.now().toString())
            .build();
    }

    public static ErrorResponse of(ErrorCode errorCode, List<ValidationError> details) {
        return ErrorResponse.builder()
            .httpStatus(errorCode.getHttpStatus().value())
            .error(ErrorDetail.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .details(details)
                .build())
            .timestamp(LocalDateTime.now().toString())
            .build();
    }

    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
            .httpStatus(errorCode.getHttpStatus().value())
            .error(ErrorDetail.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build())
            .timestamp(LocalDateTime.now().toString())
            .build();
    }
}
