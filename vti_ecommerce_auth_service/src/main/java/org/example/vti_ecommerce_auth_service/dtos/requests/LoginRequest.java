package org.example.vti_ecommerce_auth_service.dtos.requests;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

@Builder
public class LoginRequest {

    @NotEmpty(message = "Username is required")
    private String username;

    @NotEmpty(message = "password is required")
    private String password;
}
