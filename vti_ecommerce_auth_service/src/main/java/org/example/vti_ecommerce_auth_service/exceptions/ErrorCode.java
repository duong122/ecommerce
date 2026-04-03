package org.example.vti_ecommerce_auth_service.exceptions;

import org.apache.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    USER_ALREADY_EXISTS(HttpStatus.SC_CONFLICT, "User already existed"),
    REGISTER_FAILED(HttpStatus.SC_CONFLICT, "Register failed, please try again"),
    INVALID_CREDENTIAL(HttpStatus.SC_UNAUTHORIZED, "Invalid username or password"),
    INVALID_REFRESH_TOKEN(HttpStatus.SC_BAD_REQUEST, "Invalid refresh token"),
    UNAUTHORIZED(HttpStatus.SC_UNAUTHORIZED, "Invalid credentials"),
    INVALID_OLD_PASSWORD(HttpStatus.SC_BAD_REQUEST, "Old password is incorect"),
    USER_NOT_FOUND(HttpStatus.SC_NOT_FOUND, "User not found");

    private final int httpStatus;
    private final String message;

    
}
