package com.bookamore.backend.dto.password;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotNull(message = "The current password cannot be null.")
    @Schema(example = "myPassword123",
            description = "Current password. Send an empty string when the account does not have a password yet.")
    private String currentPassword;

    @NotBlank(message = "The password cannot be blank.")
    @Pattern(regexp = "(?=.*[a-z])(?=.*[A-Z]).{6,}",
            message = "The password must be at least 6 characters long " +
                    "and contain at least 1 uppercase and 1 lowercase letter")
    @Schema(example = "myNewPassword123",
            description = "The password must be at least 6 characters long and contain at least 1 uppercase and 1 lowercase letter")
    private String newPassword;
}
