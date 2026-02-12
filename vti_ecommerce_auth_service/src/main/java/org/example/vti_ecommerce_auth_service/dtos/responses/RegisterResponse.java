package org.example.vti_ecommerce_auth_service.dtos.responses;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterResponse {
    private String userId;
    private String username;
    private String message;
    private String email;
    private LocalDateTime registeredAt;
}
