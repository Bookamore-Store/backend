package com.bookamore.backend.service.impl;

import lombok.RequiredArgsConstructor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class S3ImageUrlService {

    private static final Pattern IMAGE_PATH = Pattern.compile("^/img/([^/]+)/([^/]+)$");

    private final String publicBaseUrl;

    public String toPublicUrl(String path) {
        if (path == null || path.startsWith("https://") || path.startsWith("http://")) {
            return path;
        }

        Matcher matcher = IMAGE_PATH.matcher(path);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Unsupported image path: " + path);
        }

        return publicBaseUrl + "/" + matcher.group(1) + "/" + matcher.group(2);
    }
}
