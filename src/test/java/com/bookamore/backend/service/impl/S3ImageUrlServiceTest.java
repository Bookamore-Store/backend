package com.bookamore.backend.service.impl;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class S3ImageUrlServiceTest {

    private final S3ImageUrlService service = new S3ImageUrlService(
            "https://objectstorage.example.com/n/namespace/b/bookamore/o"
    );

    @Test
    void toPublicUrl_appendsImageObjectPathToOciBaseUrl() {
        assertThat(service.toPublicUrl("/img/book/cover.jpg"))
                .isEqualTo("https://objectstorage.example.com/n/namespace/b/bookamore/o/book/cover.jpg");
    }

    @Test
    void toPublicUrl_leavesAbsoluteUrlsUnchanged() {
        String url = "https://cdn.example.com/cover.jpg";

        assertThat(service.toPublicUrl(url)).isEqualTo(url);
    }

    @Test
    void toPublicUrl_rejectsUnsupportedPaths() {
        assertThatThrownBy(() -> service.toPublicUrl("/uploads/cover.jpg"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
