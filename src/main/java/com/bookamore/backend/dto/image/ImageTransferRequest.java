package com.bookamore.backend.dto.image;

import com.bookamore.backend.service.ImageStorageType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ImageTransferRequest {

    @NotNull
    private ImageStorageType from;

    @NotNull
    private ImageStorageType to;
}
