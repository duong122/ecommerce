package org.example.vti_ecommerce_auth_service.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    USER_ALREADY_EXISTS(409, "User already existed"),
    REGISTER_FAILED(500, "Register failed, please try again"),
    INVALID_CREDENTIAL(401, "Invalid username or password"),
    INVALID_REFRESH_TOKEN(400, "Invalid refresh token"),
    UNAUTHORIZED(401, "Invalid credentials"),
    INVALID_OLD_PASSWORD(400, "Old password is incorect"),
    USER_NOT_FOUND(404, "User not found");

    private final int httpStatus;
    private final String message;
}
