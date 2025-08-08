package com.crumoria.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public final class BusinessException extends RuntimeException {

    private final ErrorCode code;
    private final Map<String, String> details;

    public BusinessException(ErrorCode code, String message) {
        this(code, message, null);
    }

    public BusinessException(ErrorCode code, String message, Map<String, String> details) {
        super(message);
        this.code = code;
        this.details = details;
    }
}