package com.bookamore.backend.controller;

import com.bookamore.backend.config.ImageStorageRoutingConfig;
import com.bookamore.backend.dto.image.ImageTransferRequest;
import com.bookamore.backend.dto.image.ImageTransferResponse;
import com.bookamore.backend.service.ImageTransferService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@RequestMapping("api/v1/images/transfer")
@ConditionalOnBean(name = {
        ImageStorageRoutingConfig.LOCAL_BEAN,
        ImageStorageRoutingConfig.S3_BEAN
})
@RequiredArgsConstructor
public class ImageTransferController {

    private final ImageTransferService imageTransferService;

    @PostMapping
    public ResponseEntity<ImageTransferResponse> transfer(@Validated @RequestBody ImageTransferRequest request) {
        return ResponseEntity.ok(imageTransferService.transfer(request));
    }
}
