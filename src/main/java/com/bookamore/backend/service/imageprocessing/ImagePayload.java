package com.bookamore.backend.service.imageprocessing;

import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Image moving through the processing pipeline.
 * {@code content} and {@code decoded} stay null while the upload has not been changed.
 */
public record ImagePayload(
        MultipartFile source,
        String fileName,
        String contentType,
        ImageFormat format,
        byte[] content,
        BufferedImage decoded
) {

    public static ImagePayload from(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        return new ImagePayload(
                file,
                fileName,
                file.getContentType(),
                ImageFormat.fromFileName(fileName),
                null,
                null
        );
    }

    public boolean untouched() {
        return content == null && decoded == null;
    }

    public byte[] bytes() throws IOException {
        if (content != null) {
            return content;
        }
        if (source == null) {
            throw new IllegalStateException("Image has no encoded bytes.");
        }
        return source.getBytes();
    }

    public ImagePayload withDecoded(BufferedImage decoded, ImageFormat format, String fileName, String contentType) {
        return new ImagePayload(null, fileName, contentType, format, null, decoded);
    }

    public ImagePayload withEncoded(String fileName, String contentType, ImageFormat format, byte[] content) {
        return new ImagePayload(null, fileName, contentType, format, content, null);
    }
}
