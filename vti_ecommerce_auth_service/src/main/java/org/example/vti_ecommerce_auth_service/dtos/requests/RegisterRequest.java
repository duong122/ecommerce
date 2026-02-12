package org.example.vti_ecommerce_auth_service.dtos.requests;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    @NotEmpty
    @Min(value = 6, message = "username must be at least 6 characters")
    private String username;

    @NotEmpty
    @Max(value = 20, message = "password must be smaller than 20 characters")
    @Pattern(
            regexp="^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#$%^&+=!])(?=\\\\S+$).{8,}$",
            message = "password must be at least 8 character, contain digit, uppercase letter and special character"
    )
    private String password;

    @NotEmpty
    private String fistName;

    @NotEmpty
    private String lastName;

    @Email
    private String email;
}
