package com.bookamore.backend.repository;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

public interface ImageStorageRepository {

    void saveImage(MultipartFile file, String fileName, String subDir) throws IOException;

    boolean isExists(String fileName, String subDir);

    boolean deleteImage(String fileName, String subDir) throws IOException;

    Optional<byte[]> getImage(String fileName, String subDir) throws IOException;
}
