package com.bookamore.backend.listener;

import com.bookamore.backend.entity.Image;
import com.bookamore.backend.repository.ImageStorageRepository;
import jakarta.persistence.PreRemove;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageStorageCleanupListener {

    private static final Pattern SUB_DIR_PATTERN = Pattern.compile("^/img/(.+)/.+$");
    private static final Pattern FILE_NAME_PATTERN = Pattern.compile("^/img/.+/(.+)$");

    private final ApplicationEventPublisher eventPublisher;
    private final ImageStorageRepository imageStorageRepository;

    @PreRemove
    public void onRemove(Image image) {
        String path = image.getPath();
        UUID imageId = image.getId();
        eventPublisher.publishEvent(new ImageRemovedEvent(
                extractPathPart(FILE_NAME_PATTERN, path, "file name", imageId),
                extractPathPart(SUB_DIR_PATTERN, path, "subdirectory", imageId)
        ));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void deleteFile(ImageRemovedEvent event) {
        try {
            imageStorageRepository.deleteImage(event.fileName(), event.subDir());
        } catch (IOException e) {
            log.error("Failed to delete image file after commit: subDir={}, fileName={}",
                    event.subDir(), event.fileName(), e);
        }
    }

    private String extractPathPart(Pattern pattern, String path, String part, UUID imageId) {
        if (path == null) {
            log.warn("Failed to extract {} from path: path is null, imageId='{}'", part, imageId);
            throw new RuntimeException("Failed to extract " + part + " from path!");
        }
        return pattern.matcher(path)
                .results()
                .map(match -> match.group(1))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Failed to extract {} from string='{}', imageId='{}'", part, path, imageId);
                    return new RuntimeException("Failed to extract " + part + " from path!");
                });
    }
}
