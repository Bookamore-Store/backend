package com.bookamore.backend.controller;

import com.bookamore.backend.dto.image.ImageRequest;
import com.bookamore.backend.dto.image.ImageResponse;
import com.bookamore.backend.entity.enums.EntityType;
import com.bookamore.backend.service.ImageService;
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

    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "Authenticated user is not the author"),
            @ApiResponse(responseCode = "404", description = "Offer or book not found"),
            @ApiResponse(responseCode = "409", description = "Entity has reached the maximum number of images")
    })
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ImageResponse> uploadImage(@ModelAttribute ImageRequest request) {
        return ResponseEntity.ok(imageService.save(request));
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
