package com.bookamore.backend.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileUpdateRequest {
    @Size(min = 2, max = 100, message = "The name must consist of at least 2 characters")
    @Schema(example = "John Doe", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String name;

    @AssertTrue(message = "At least one profile field must be provided.")
    @Schema(hidden = true)
    public boolean isAnyFieldPresent() {
        return name != null;
    }
}
