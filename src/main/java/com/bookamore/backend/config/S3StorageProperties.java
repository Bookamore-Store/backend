package com.bookamore.backend.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@ToString(exclude = {"secretKey", "accessKey"})
@Validated
@ConfigurationProperties(prefix = "file.s3")
public class S3StorageProperties {

    /**
     * Optional. Empty means the AWS default endpoint for {@link #region}.
     * Set this for MinIO, R2, Spaces and other S3-compatible services.
     */
    private String endpoint;

    @NotBlank
    private String region;

    @NotBlank
    private String bucket;

    @NotBlank
    private String accessKey;

    @NotBlank
    private String secretKey;

    /**
     * Path-style URLs ({@code endpoint/bucket/key}) are required by most
     * S3-compatible servers. Virtual-hosted style is AWS S3's default.
     */
    private boolean pathStyleAccess = true;
}
