package com.bookamore.backend.service.impl;

import com.bookamore.backend.dto.image.ImageTransferRequest;
import com.bookamore.backend.dto.image.ImageTransferResponse;
import com.bookamore.backend.entity.Image;
import com.bookamore.backend.exception.UnprocessableRequestException;
import com.bookamore.backend.repository.ImageRepository;
import com.bookamore.backend.repository.ImageStorageRepository;
import com.bookamore.backend.service.ImageStorageRegistry;
import com.bookamore.backend.service.ImageStorageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageTransferServiceImplTest {

    @Mock
    private ImageRepository imageRepository;
    @Mock
    private ImageStorageRepository localStorage;
    @Mock
    private ImageStorageRepository s3Storage;

    private ImageTransferServiceImpl service;

    @BeforeEach
    void setUp() {
        ImageStorageRegistry registry = ImageStorageRegistry.of(
                ImageStorageType.LOCAL,
                localStorage,
                s3Storage
        );
        service = new ImageTransferServiceImpl(imageRepository, registry);
    }

    @Test
    void transfer_copiesMissingFilesAndDoesNotDelete() throws IOException {
        Image image = image("/img/book/cover.jpg");
        when(imageRepository.findAll()).thenReturn(List.of(image));
        when(s3Storage.isExists("cover.jpg", "book")).thenReturn(false);
        when(localStorage.getImage("cover.jpg", "book")).thenReturn(Optional.of(new byte[]{1, 2, 3}));

        ImageTransferResponse response = service.transfer(request(ImageStorageType.LOCAL, ImageStorageType.S3));

        assertThat(response.getCopied()).isEqualTo(1);
        assertThat(response.getScanned()).isEqualTo(1);
        verify(s3Storage).saveImage(any(MultipartFile.class), eq("cover.jpg"), eq("book"));
        verify(localStorage, never()).deleteImage(any(), any());
        verify(s3Storage, never()).deleteImage(any(), any());
    }

    @Test
    void transfer_skipsWhenDestinationAlreadyHasFile() throws IOException {
        when(imageRepository.findAll()).thenReturn(List.of(image("/img/book/cover.jpg")));
        when(s3Storage.isExists("cover.jpg", "book")).thenReturn(true);

        ImageTransferResponse response = service.transfer(request(ImageStorageType.LOCAL, ImageStorageType.S3));

        assertThat(response.getSkipped()).isEqualTo(1);
        verify(localStorage, never()).getImage(any(), any());
        verify(s3Storage, never()).saveImage(any(), any(), any());
    }

    @Test
    void transfer_countsMissingWhenSourceHasNoFile() throws IOException {
        when(imageRepository.findAll()).thenReturn(List.of(image("/img/offer/photo.webp")));
        when(s3Storage.isExists("photo.webp", "offer")).thenReturn(false);
        when(localStorage.getImage("photo.webp", "offer")).thenReturn(Optional.empty());

        ImageTransferResponse response = service.transfer(request(ImageStorageType.LOCAL, ImageStorageType.S3));

        assertThat(response.getMissing()).isEqualTo(1);
        verify(s3Storage, never()).saveImage(any(), any(), any());
    }

    @Test
    void transfer_rejectsSameSourceAndDestination() {
        assertThatThrownBy(() -> service.transfer(request(ImageStorageType.LOCAL, ImageStorageType.LOCAL)))
                .isInstanceOf(UnprocessableRequestException.class);
    }

    private static ImageTransferRequest request(ImageStorageType from, ImageStorageType to) {
        ImageTransferRequest request = new ImageTransferRequest();
        request.setFrom(from);
        request.setTo(to);
        return request;
    }

    private static Image image(String path) {
        Image image = new Image();
        image.setId(UUID.randomUUID());
        image.setPath(path);
        return image;
    }
}
