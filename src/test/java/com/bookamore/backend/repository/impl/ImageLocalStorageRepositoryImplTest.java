package com.bookamore.backend.repository.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ImageLocalStorageRepositoryImplTest {

    @TempDir
    Path uploadDir;

    private ImageLocalStorageRepositoryImpl storage;

    @BeforeEach
    void setUp() {
        storage = new ImageLocalStorageRepositoryImpl(uploadDir);
    }

    @Test
    void getImage_returnsBytesWhenFileExists() throws IOException {
        byte[] bytes = {1, 2, 3};
        Path bookDir = uploadDir.resolve("book");
        Files.createDirectories(bookDir);
        Files.write(bookDir.resolve("cover.jpg"), bytes);

        assertThat(storage.getImage("cover.jpg", "book")).contains(bytes);
    }

    @Test
    void getImage_returnsEmptyWhenFileMissing() throws IOException {
        assertThat(storage.getImage("missing.jpg", "book")).isEmpty();
    }
}
