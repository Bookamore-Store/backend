package com.bookamore.backend.service.imageprocessing.formatreader.impl;

import com.bookamore.backend.exception.NotValidImageContentException;
import com.bookamore.backend.service.imageprocessing.formatreader.FormatReader;
import com.bookamore.backend.service.imageprocessing.ImageFormat;
import com.bookamore.backend.service.imageprocessing.ImagePayload;
import com.bookamore.backend.service.imageprocessing.decoder.HeicDecoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.awt.image.BufferedImage;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class HeicFormatReader implements FormatReader {

    private final HeicDecoder heicDecoder;

    @Override
    public boolean supports(ImageFormat format) {
        return format == ImageFormat.HEIC || format == ImageFormat.HEIF;
    }

    @Override
    public ImagePayload read(ImagePayload image) {
        try {
            BufferedImage decoded = heicDecoder.decode(image.bytes());
            return image.withDecoded(decoded, ImageFormat.WEBP, webpFileName(image.fileName()), "image/webp");
        } catch (IOException e) {
            throw new NotValidImageContentException("Unable to read HEIC image. Please upload a valid image.", e);
        }
    }

    private static String webpFileName(String originalFileName) {
        String cleaned = StringUtils.cleanPath(originalFileName);
        int dot = cleaned.lastIndexOf('.');
        String base = dot > 0 ? cleaned.substring(0, dot) : cleaned;
        if (!StringUtils.hasText(base)) {
            base = "image";
        }
        return base + ".webp";
    }
}
