package org.example.vti_ecommerce_auth_service.dtos.responses;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ResetpasswordResponse {
    
    @NotEmpty(message = "Username must not be empty")
    private String username;

    @NotEmpty(message = "Message must not be empty")
    private String message;
}
