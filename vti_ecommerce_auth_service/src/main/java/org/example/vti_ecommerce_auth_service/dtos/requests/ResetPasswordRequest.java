package org.example.vti_ecommerce_auth_service.dtos.requests;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ResetPasswordRequest {

    @NotEmpty(message = "Username must not be empty")
    private String username;

    @NotEmpty(message = "Old password must not be empty")
    private String oldPassword;

    @Pattern(
        regexp="^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#$%^&+=!])(?=\\\\S+$).{8,}$",
        message = "password must be at least 8 character, contain digit, uppercase letter and special character"
    )
    private String newPassword;
}
