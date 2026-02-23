package org.example.vti_ecommerce_auth_service.dtos.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshTokenRequest {
    private String refreshToken;
}
