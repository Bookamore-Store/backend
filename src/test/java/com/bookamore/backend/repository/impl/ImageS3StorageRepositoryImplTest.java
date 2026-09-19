package com.bookamore.backend.repository.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageS3StorageRepositoryImplTest {

    private static final String BUCKET = "bookamore";

    @Mock
    private S3Client s3Client;
    @Mock
    private MultipartFile file;

    private ImageS3StorageRepositoryImpl storage;

    @BeforeEach
    void setUp() {
        storage = new ImageS3StorageRepositoryImpl(s3Client, BUCKET);
    }

    @Test
    void saveImage_putsObjectUnderEntitySubdir() throws IOException {
        byte[] bytes = {1, 2, 3};
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(bytes));
        when(file.getSize()).thenReturn((long) bytes.length);
        when(file.getContentType()).thenReturn("image/jpeg");
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        storage.saveImage(file, "cover.jpg", "book");

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
        assertThat(captor.getValue().bucket()).isEqualTo(BUCKET);
        assertThat(captor.getValue().key()).isEqualTo("img/book/cover.jpg");
        assertThat(captor.getValue().contentType()).isEqualTo("image/jpeg");
    }

    @Test
    void isExists_returnsFalseWhenObjectMissing() {
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(NoSuchKeyException.builder().statusCode(404).build());

        assertThat(storage.isExists("missing.jpg", "book")).isFalse();
    }

    @Test
    void isExists_returnsTrueWhenHeadSucceeds() {
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenReturn(HeadObjectResponse.builder().build());

        assertThat(storage.isExists("cover.jpg", "book")).isTrue();
    }

    @Test
    void getImage_returnsBytesWhenObjectExists() throws IOException {
        byte[] bytes = {1, 2, 3};
        when(s3Client.getObject(any(GetObjectRequest.class)))
                .thenReturn(new ResponseInputStream<>(
                        GetObjectResponse.builder().build(),
                        AbortableInputStream.create(new ByteArrayInputStream(bytes))
                ));

        assertThat(storage.getImage("cover.jpg", "book")).contains(bytes);

        ArgumentCaptor<GetObjectRequest> captor = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(s3Client).getObject(captor.capture());
        assertThat(captor.getValue().bucket()).isEqualTo(BUCKET);
        assertThat(captor.getValue().key()).isEqualTo("img/book/cover.jpg");
    }

    @Test
    void getImage_returnsEmptyWhenObjectMissing() throws IOException {
        when(s3Client.getObject(any(GetObjectRequest.class)))
                .thenThrow(NoSuchKeyException.builder().statusCode(404).build());

        assertThat(storage.getImage("missing.jpg", "book")).isEmpty();
    }

    @Test
    void deleteImage_returnsFalseWhenObjectAlreadyGone() throws IOException {
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(NoSuchKeyException.builder().statusCode(404).build());

        assertThat(storage.deleteImage("gone.jpg", "offer")).isFalse();

        ArgumentCaptor<DeleteObjectRequest> captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(captor.capture());
        assertThat(captor.getValue().key()).isEqualTo("img/offer/gone.jpg");
    }
}
