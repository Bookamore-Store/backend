package com.bookamore.backend.repository;

import com.bookamore.backend.service.ImageStorageRegistry;
import com.bookamore.backend.service.ImageStorageType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class ImageStorageRegistryTest {

    @Mock
    private ImageStorageRepository localStorage;
    @Mock
    private ImageStorageRepository s3Storage;

    @Test
    void of_whenBothPresent_selectsWriteTargetAndKeepsTheOtherAvailable() {
        ImageStorageRegistry registry = ImageStorageRegistry.of(
                ImageStorageType.LOCAL,
                localStorage,
                s3Storage
        );

        assertThat(registry.writeStorage()).isSameAs(localStorage);
        assertThat(registry.getAvailable()).containsOnlyKeys(ImageStorageType.LOCAL, ImageStorageType.S3);
        assertThat(registry.get(ImageStorageType.S3)).contains(s3Storage);
    }

    @Test
    void of_whenWriteTargetMissing_failsStartup() {
        assertThatThrownBy(() -> ImageStorageRegistry.of(ImageStorageType.S3, localStorage, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ACTIVE_FILE_STORAGE=s3");
    }

    @Test
    void of_whenNoStorageActive_failsStartup() {
        assertThatThrownBy(() -> ImageStorageRegistry.of(ImageStorageType.LOCAL, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No image storage is active");
    }
}
