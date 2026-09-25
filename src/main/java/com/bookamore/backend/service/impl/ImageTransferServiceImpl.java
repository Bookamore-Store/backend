package com.bookamore.backend.service.impl;

import com.bookamore.backend.config.ImageStorageRoutingConfig;
import com.bookamore.backend.dto.image.ImageTransferRequest;
import com.bookamore.backend.dto.image.ImageTransferResponse;
import com.bookamore.backend.entity.Image;
import com.bookamore.backend.exception.UnprocessableRequestException;
import com.bookamore.backend.repository.ImageRepository;
import com.bookamore.backend.repository.ImageStorageRepository;
import com.bookamore.backend.service.ImageStorageRegistry;
import com.bookamore.backend.service.ImageStorageType;
import com.bookamore.backend.service.ImageTransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@ConditionalOnBean(name = {
        ImageStorageRoutingConfig.LOCAL_BEAN,
        ImageStorageRoutingConfig.S3_BEAN
})
@RequiredArgsConstructor
public class ImageTransferServiceImpl implements ImageTransferService {

    private static final Pattern IMAGE_PATH = Pattern.compile("^/img/([^/]+)/([^/]+)$");

    private final ImageRepository imageRepository;
    private final ImageStorageRegistry imageStorageRegistry;

    @Override
    public ImageTransferResponse transfer(ImageTransferRequest request) {
        ImageStorageType from = request.getFrom();
        ImageStorageType to = request.getTo();
        if (from == to) {
            throw new UnprocessableRequestException("from and to must be different");
        }

        ImageStorageRepository source = storage(from);
        ImageStorageRepository destination = storage(to);

        int copied = 0;
        int skipped = 0;
        int missing = 0;
        int failed = 0;

        List<Image> images = imageRepository.findAll();
        for (Image image : images) {
            Matcher matcher = IMAGE_PATH.matcher(image.getPath() == null ? "" : image.getPath());
            if (!matcher.matches()) {
                failed++;
                continue;
            }
            String subDir = matcher.group(1);
            String fileName = matcher.group(2);
            try {
                if (destination.isExists(fileName, subDir)) {
                    skipped++;
                    continue;
                }
                Optional<byte[]> data = source.getImage(fileName, subDir);
                if (data.isEmpty()) {
                    missing++;
                    continue;
                }
                destination.saveImage(
                        new BytesMultipartFile(fileName, contentType(fileName), data.get()),
                        fileName,
                        subDir
                );
                copied++;
            } catch (Exception e) {
                failed++;
                log.error("Failed to copy image {} ({})", image.getId(), image.getPath(), e);
            }
        }

        log.info("Image transfer {} -> {}: scanned={}, copied={}, skipped={}, missing={}, failed={}",
                from, to, images.size(), copied, skipped, missing, failed);

        return ImageTransferResponse.builder()
                .scanned(images.size())
                .copied(copied)
                .skipped(skipped)
                .missing(missing)
                .failed(failed)
                .build();
    }

    private ImageStorageRepository storage(ImageStorageType type) {
        return imageStorageRegistry.get(type)
                .orElseThrow(() -> new UnprocessableRequestException(type + " storage is not enabled"));
    }

    private static String contentType(String fileName) {
        String name = fileName.toLowerCase();
        if (name.endsWith(".png")) {
            return "image/png";
        }
        if (name.endsWith(".webp")) {
            return "image/webp";
        }
        return "image/jpeg";
    }

    private static final class BytesMultipartFile implements MultipartFile {

        private final String fileName;
        private final String contentType;
        private final byte[] content;

        private BytesMultipartFile(String fileName, String contentType, byte[] content) {
            this.fileName = fileName;
            this.contentType = contentType;
            this.content = content;
        }

        @Override
        public String getName() {
            return "file";
        }

        @Override
        public String getOriginalFilename() {
            return fileName;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() {
            return content;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(File dest) throws IOException {
            Files.write(dest.toPath(), content);
        }
    }
}
