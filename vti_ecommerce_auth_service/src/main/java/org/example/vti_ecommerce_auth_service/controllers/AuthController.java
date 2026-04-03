package org.example.vti_ecommerce_auth_service.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.example.vti_ecommerce_auth_service.dtos.requests.RefreshTokenRequest;
import org.example.vti_ecommerce_auth_service.dtos.requests.RegisterRequest;
import org.example.vti_ecommerce_auth_service.dtos.requests.ResetPasswordRequest;
import org.example.vti_ecommerce_auth_service.dtos.requests.TokenRequest;
import org.example.vti_ecommerce_auth_service.dtos.responses.BaseResponse;
import org.example.vti_ecommerce_auth_service.dtos.responses.RegisterResponse;
import org.example.vti_ecommerce_auth_service.dtos.responses.ResetpasswordResponse;
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
    @PostMapping("/refresh")
    ResponseEntity<BaseResponse<TokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        return ResponseEntity.ok(
                BaseResponse.<TokenResponse>builder()
                        .success(true)
                        .message("Get access token again successfully")
                        .data(authService.refreshToken(request))
                        .timestamp(LocalDateTime.now())
                        .fieldErrors(null)
                        .build()
        );
    }

    // Reset password
    @PostMapping("/reset-password")
    ResponseEntity<BaseResponse<ResetpasswordResponse>> resetPassword(
        @Valid @RequestBody ResetPasswordRequest request
    ) {
        return ResponseEntity.ok(
                BaseResponse.<ResetpasswordResponse>builder()
                                .success(true)
                                .message("Reset passwrod for user " + request.getUsername() + " successfully")
                                .data(authService.resetPassword(request))
                                .timestamp(LocalDateTime.now())
                                .fieldErrors(null)
                                .build()
        );
    }
    
}




