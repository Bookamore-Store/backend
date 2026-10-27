package com.bookamore.backend.dto.user;

import com.bookamore.backend.dto.image.ImageShortResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
public class UserResponse {
    @Schema(example = "d367ec63-a73b-485d-a1c8-6b793b009e53")
    private UUID id;

    @Schema(example = "John")
    private String name;

    @Schema(example = "john@example.com")
    private String email;

    @Schema(description = "Avatar URL provided by the social login provider (Google or Facebook)",
            example = "https://oauth-provider.com/img-data",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String socialAvatarUrl;

    @Schema(description = "Uploaded profile image; omitted when no image is uploaded",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ImageShortResponse uploadedImage;
}
