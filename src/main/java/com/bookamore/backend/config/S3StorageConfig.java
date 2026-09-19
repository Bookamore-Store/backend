package com.bookamore.backend.config;

import com.bookamore.backend.repository.ImageStorageRepository;
import com.bookamore.backend.repository.impl.ImageS3StorageRepositoryImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "file.storage", name = "s3-enabled", havingValue = "true")
@EnableConfigurationProperties(S3StorageProperties.class)
@RequiredArgsConstructor
public class S3StorageConfig {

    private final S3StorageProperties properties;

    @Bean(destroyMethod = "close")
    public S3Client s3Client() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                properties.getAccessKey(),
                properties.getSecretKey()
        );

        var builder = S3Client.builder()
                .region(Region.of(properties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(properties.isPathStyleAccess())
                        .build())
                // AWS SDK 2.30+ default checksums break many S3-compatible servers.
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED);

        if (StringUtils.hasText(properties.getEndpoint())) {
            builder.endpointOverride(URI.create(properties.getEndpoint()));
        }

        log.info("S3 image storage available: bucket={}, region={}, endpoint={}, pathStyle={}",
                properties.getBucket(),
                properties.getRegion(),
                StringUtils.hasText(properties.getEndpoint()) ? properties.getEndpoint() : "(aws default)",
                properties.isPathStyleAccess());

        return builder.build();
    }

    @Bean(ImageStorageRoutingConfig.S3_BEAN)
    public ImageStorageRepository s3ImageStorageRepository(S3Client s3Client) {
        return new ImageS3StorageRepositoryImpl(s3Client, properties.getBucket());
    }
}
