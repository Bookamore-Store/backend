package com.bookamore.backend.dto.password;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequest {
    @NotBlank(message = "The email cannot be blank.")
    @Email(message = "Invalid email format")
    @Schema(example = "john@example.com")
    private String email;
}
