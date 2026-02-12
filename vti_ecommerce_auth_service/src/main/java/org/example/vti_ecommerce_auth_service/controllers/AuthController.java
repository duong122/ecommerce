package org.example.vti_ecommerce_auth_service.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.vti_ecommerce_auth_service.dtos.requests.RegisterRequest;
import org.example.vti_ecommerce_auth_service.dtos.requests.TokenRequest;
import org.example.vti_ecommerce_auth_service.dtos.responses.BaseResponse;
import org.example.vti_ecommerce_auth_service.dtos.responses.RegisterResponse;
import org.example.vti_ecommerce_auth_service.dtos.responses.TokenResponse;
import org.example.vti_ecommerce_auth_service.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@Validated
@RequestMapping("/v1/api/auth/")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Register
    @PostMapping("/register")
    ResponseEntity<BaseResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(
                BaseResponse.<RegisterResponse>builder()
                        .success(true)
                        .message("User register successfully")
                        .data(authService.register(request))
                        .timestamp(LocalDateTime.now())
                        .fieldErrors(null)
                        .build()
        );
    }

    // Get access token from password
    @PostMapping("/login")
    ResponseEntity<BaseResponse<TokenResponse>> login(
            @Valid @RequestBody TokenRequest request
            ) {
        return ResponseEntity.ok(
                BaseResponse.<TokenResponse>builder()
                        .success(true)
                        .message("Get access token successfully")
                        .data(authService.login(request))
                        .timestamp(LocalDateTime.now())
                        .fieldErrors(null)
                        .build()
        );
    }
    // Get access token from refresh token


    // Reset password

}
