package com.bookamore.backend.repository.impl;

import com.bookamore.backend.repository.ImageStorageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class ImageS3StorageRepositoryImpl implements ImageStorageRepository {

    private final S3Client s3Client;
    private final String bucket;

    @Override
    public void saveImage(MultipartFile file, String fileName, String subDir) throws IOException {
        String objectKey = objectKey(subDir, fileName);
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();
            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            log.info("File saved successfully to S3: s3://{}/{}", bucket, objectKey);
        } catch (SdkException e) {
            log.error("Failed to save file to S3: s3://{}/{}", bucket, objectKey, e);
            throw new IOException("Failed to save image to S3: " + objectKey, e);
        }
    }

    @Override
    public boolean isExists(String fileName, String subDir) {
        String objectKey = objectKey(subDir, fileName);
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public boolean deleteImage(String fileName, String subDir) throws IOException {
        String objectKey = objectKey(subDir, fileName);
        boolean existed = isExists(fileName, subDir);
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build());
            if (existed) {
                log.info("File deleted successfully from S3: s3://{}/{}", bucket, objectKey);
            } else {
                log.warn("File does not exist in S3; nothing to delete: s3://{}/{}", bucket, objectKey);
            }
            return existed;
        } catch (SdkException e) {
            log.error("Failed to delete file from S3: s3://{}/{}", bucket, objectKey, e);
            throw new IOException("Failed to delete image from S3: " + objectKey, e);
        }
    }

    private static String objectKey(String subDir, String fileName) {
        return "img/" + subDir + "/" + fileName;
    }
}
