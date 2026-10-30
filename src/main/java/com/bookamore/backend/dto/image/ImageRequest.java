package com.bookamore.backend.dto.image;

import com.bookamore.backend.entity.enums.EntityType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Data
public class ImageRequest {

    @NotNull(message = "Entity type is required!")
    @Schema(
        description = "Entity type of image",
        implementation = String.class,
        allowableValues = {"BOOK", "USER"},
        example = "BOOK"
    )
    private EntityType entityType;

    @Schema(
        description = "Required for BOOK. For USER, defaults to the authenticated user's ID when omitted.",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        example = "018d4f1a-5b03-71d4-c001-000000000001"
    )
    private UUID entityId;

    @NotNull(message = "File is required!")
    @Schema(description = "Image file")
    private MultipartFile file;

    @Size(min = 1, max = 500, message = "Description can be up to 500 characters.")
    @Schema(description = "Image description", example = "Book cover")
    private String description;

    @Schema(
            description = "Display order of the image. Defaults to 0 when omitted.",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private int orderIndex = -1;
}
