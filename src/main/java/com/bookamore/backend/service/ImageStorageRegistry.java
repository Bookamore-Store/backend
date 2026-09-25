package com.bookamore.backend.service;

import com.bookamore.backend.repository.ImageStorageRepository;
import lombok.Getter;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

@Getter
public class ImageStorageRegistry {

    private final ImageStorageType writeTarget;
    private final Map<ImageStorageType, ImageStorageRepository> available;

    private ImageStorageRegistry(
            ImageStorageType writeTarget,
            Map<ImageStorageType, ImageStorageRepository> available
    ) {
        this.writeTarget = writeTarget;
        this.available = Collections.unmodifiableMap(available);
    }

    public static ImageStorageRegistry of(
            ImageStorageType writeTarget,
            ImageStorageRepository localStorage,
            ImageStorageRepository s3Storage
    ) {
        if (writeTarget == null) {
            throw new IllegalStateException("file.storage.write must be set to local or s3");
        }

        Map<ImageStorageType, ImageStorageRepository> available = new EnumMap<>(ImageStorageType.class);
        if (localStorage != null) {
            available.put(ImageStorageType.LOCAL, localStorage);
        }
        if (s3Storage != null) {
            available.put(ImageStorageType.S3, s3Storage);
        }

        if (available.isEmpty()) {
            throw new IllegalStateException(
                    "No image storage is active. Set FILE_STORAGE_LOCAL=true and/or FILE_STORAGE_S3=true."
            );
        }

        if (!available.containsKey(writeTarget)) {
            throw new IllegalStateException(
                    "ACTIVE_FILE_STORAGE=" + writeTarget.name().toLowerCase()
                            + " but that storage is disabled. Available: "
                            + available.keySet()
            );
        }

        return new ImageStorageRegistry(writeTarget, available);
    }

    public ImageStorageRepository writeStorage() {
        return available.get(writeTarget);
    }

    public Optional<ImageStorageRepository> get(ImageStorageType type) {
        return Optional.ofNullable(available.get(type));
    }
}
