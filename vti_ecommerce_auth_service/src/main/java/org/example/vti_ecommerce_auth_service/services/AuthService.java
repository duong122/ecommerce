package org.example.vti_ecommerce_auth_service.services;

import org.example.vti_ecommerce_auth_service.dtos.requests.RefreshTokenRequest;
import org.example.vti_ecommerce_auth_service.dtos.requests.RegisterRequest;
import org.example.vti_ecommerce_auth_service.dtos.requests.TokenRequest;
import org.example.vti_ecommerce_auth_service.dtos.responses.RegisterResponse;
import org.example.vti_ecommerce_auth_service.dtos.responses.TokenResponse;

public interface AuthService {
    RegisterResponse register(RegisterRequest request);

    TokenResponse login(TokenRequest request);

    TokenResponse refreshToken(RefreshTokenRequest request);
}
