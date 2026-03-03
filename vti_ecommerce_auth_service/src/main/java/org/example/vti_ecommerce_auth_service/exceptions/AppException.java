package org.example.vti_ecommerce_auth_service.exceptions;

import java.util.Map;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException{
    private final ErrorCode errorCode;
    private final Map<String, String> fieldErrors;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.fieldErrors = null;
    }

    public AppException(ErrorCode errorCode, Map<String, String> fieldsErrors) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.fieldErrors = fieldsErrors;
    }

    public AppException(ErrorCode errorCode, String message, Map<String, String> fieldsErrors) {
        super(message);
        this.errorCode = errorCode;
        this.fieldErrors = fieldsErrors;
    }
}
