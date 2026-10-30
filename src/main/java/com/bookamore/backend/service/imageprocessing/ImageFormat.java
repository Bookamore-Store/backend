package com.bookamore.backend.service.imageprocessing;

import com.bookamore.backend.exception.UnsupportedImageTypeException;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public enum ImageFormat {
    JPEG("image/jpeg", "jpg", "jpeg"),
    PNG("image/png", "png"),
    WEBP("image/webp", "webp"),
    HEIC("image/heic", "heic"),
    HEIF("image/heif", "heif");

    private static final Map<String, ImageFormat> BY_EXTENSION;
    private static final Set<String> CONTENT_TYPES;
    private static final Set<String> EXTENSIONS;

    static {
        Map<String, ImageFormat> byExtension = new LinkedHashMap<>();
        Set<String> contentTypes = new LinkedHashSet<>();
        Set<String> extensions = new LinkedHashSet<>();
        for (ImageFormat format : values()) {
            contentTypes.add(format.contentType);
            for (String extension : format.extensions) {
                byExtension.put(extension, format);
                extensions.add(extension);
            }
        }
        BY_EXTENSION = Map.copyOf(byExtension);
        CONTENT_TYPES = Collections.unmodifiableSet(contentTypes);
        EXTENSIONS = Collections.unmodifiableSet(extensions);
    }

    private final String contentType;
    private final List<String> extensions;

    ImageFormat(String contentType, String... extensions) {
        this.contentType = contentType;
        this.extensions = List.of(extensions);
    }

    public static Set<String> contentTypes() {
        return CONTENT_TYPES;
    }

    public static Set<String> extensions() {
        return EXTENSIONS;
    }

    /**
     * Some clients upload HEIC/HEIF as {@code application/octet-stream}.
     */
    public static boolean allowsOctetStream(String extension) {
        ImageFormat format = BY_EXTENSION.get(extension);
        return format == HEIC || format == HEIF;
    }

    public static ImageFormat fromFileName(String fileName) {
        ImageFormat format = BY_EXTENSION.get(extensionOf(fileName));
        if (format == null) {
            throw new UnsupportedImageTypeException(
                    "Invalid file extension. Allowed extensions: " + String.join(", ", EXTENSIONS) + ".");
        }
        return format;
    }

    public static String extensionOf(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase();
    }
}
