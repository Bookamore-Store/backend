package com.bookamore.backend.dto.password;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "The email cannot be blank.")
    @Email(message = "Invalid email format")
    @Schema(example = "john@example.com")
    private String email;

    @NotBlank(message = "The secret code cannot be blank.")
    @Schema(example = "482913")
    private String code;

    @NotBlank(message = "The password cannot be blank.")
    @Pattern(regexp = "(?=.*[a-z])(?=.*[A-Z]).{6,}",
            message = "The password must be at least 6 characters long " +
                    "and contain at least 1 uppercase and 1 lowercase letter")
    @Schema(example = "myPassword123",
            description = "The password must be at least 6 characters long and contain at least 1 uppercase and 1 lowercase letter")
    private String password;
}
