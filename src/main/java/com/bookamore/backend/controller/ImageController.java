package com.bookamore.backend.controller;

import com.bookamore.backend.dto.image.ImageRequest;
import com.bookamore.backend.dto.image.ImageResponse;
import com.bookamore.backend.entity.enums.EntityType;
import com.bookamore.backend.service.ImageService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("api/v1/images")
@RequiredArgsConstructor
public class ImageController {
    private final ImageService imageService;

    @Hidden
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Deprecated
    public ResponseEntity<ImageResponse> uploadImageOld(@ModelAttribute ImageRequest request) {
        return ResponseEntity.ok(imageService.save(request));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Unsupported entity type or invalid image content"),
            @ApiResponse(responseCode = "403", description = "Authenticated user is not the author"),
            @ApiResponse(responseCode = "404", description = "Entity not found"),
            @ApiResponse(responseCode = "409", description = "Entity has reached the maximum number of images"),
            @ApiResponse(responseCode = "413", description = "Image or upload request exceeds the maximum size"),
            @ApiResponse(responseCode = "415", description = "Unsupported image format or extension"),
            @ApiResponse(responseCode = "422", description = "Image does not meet minimum size or dimension requirements")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageResponse> uploadImage(@ModelAttribute ImageRequest request) {
        return ResponseEntity.ok(imageService.save(request));
    }

    @Operation(summary = "Get image by ID")
    @ApiResponse(responseCode = "404", description = "Image not found")
    @GetMapping("/{imageId}")
    public ResponseEntity<ImageResponse> getById(@PathVariable UUID imageId) {
        return ResponseEntity.ok(imageService.getById(imageId));
    }

    @GetMapping(value = "/getByEntity")
    public ResponseEntity<List<ImageResponse>> getImageByEntity(@RequestParam EntityType entityType,
                                                                @RequestParam UUID entityId) {
        return ResponseEntity.ok(imageService.getImageByEntity(entityType, entityId));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "Authenticated user is not the author"),
            @ApiResponse(responseCode = "404", description = "Image, offer, or book not found")
    })
    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable UUID imageId) {
        imageService.deleteImage(imageId);
        return ResponseEntity.noContent().build();
    }

}
