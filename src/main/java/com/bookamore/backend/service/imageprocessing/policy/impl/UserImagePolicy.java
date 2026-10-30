package com.bookamore.backend.service.imageprocessing.policy.impl;

import com.bookamore.backend.entity.enums.EntityType;
import com.bookamore.backend.exception.InvalidImageSizeException;
import com.bookamore.backend.exception.NotValidImageContentException;
import com.bookamore.backend.service.imageprocessing.policy.EntityImagePolicy;
import com.bookamore.backend.service.imageprocessing.ImageFormat;
import com.bookamore.backend.service.imageprocessing.ImagePayload;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

@Component
public class UserImagePolicy implements EntityImagePolicy {

    private static final int PROFILE_IMAGE_DIMENSION = 400;

    @Override
    public EntityType entityType() {
        return EntityType.USER;
    }

    @Override
    public ImagePayload apply(ImagePayload image) {
        if (image.decoded() != null) {
            return resizeDecoded(image);
        }
        return resizeEncoded(image);
    }

    private ImagePayload resizeDecoded(ImagePayload image) {
        BufferedImage decoded = image.decoded();
        int width = decoded.getWidth();
        int height = decoded.getHeight();
        requireProfileDimensions(width, height);
        if (width == PROFILE_IMAGE_DIMENSION && height == PROFILE_IMAGE_DIMENSION) {
            return image;
        }
        BufferedImage resized = centerCropAndScale(decoded);
        return image.withDecoded(resized, image.format(), image.fileName(), image.contentType());
    }

    private ImagePayload resizeEncoded(ImagePayload image) {
        try (InputStream input = open(image);
             ImageInputStream imageInput = new MemoryCacheImageInputStream(input)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInput);
            if (!readers.hasNext()) {
                throw new NotValidImageContentException("Unable to read image dimensions. Please upload a valid image.");
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(imageInput);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                requireProfileDimensions(width, height);
                if (width == PROFILE_IMAGE_DIMENSION && height == PROFILE_IMAGE_DIMENSION) {
                    return image;
                }

                ImageReadParam readParam = reader.getDefaultReadParam();
                int cropSize = Math.min(width, height);
                readParam.setSourceRegion(new Rectangle(
                        (width - cropSize) / 2, (height - cropSize) / 2, cropSize, cropSize));
                int subsampling = Math.min(width / PROFILE_IMAGE_DIMENSION, height / PROFILE_IMAGE_DIMENSION);
                readParam.setSourceSubsampling(subsampling, subsampling, 0, 0);
                BufferedImage source = reader.read(0, readParam);
                boolean jpeg = "JPEG".equalsIgnoreCase(reader.getFormatName());
                BufferedImage resized = new BufferedImage(PROFILE_IMAGE_DIMENSION, PROFILE_IMAGE_DIMENSION,
                        jpeg ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics = resized.createGraphics();
                try {
                    graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    graphics.drawImage(source, 0, 0, PROFILE_IMAGE_DIMENSION, PROFILE_IMAGE_DIMENSION, null);
                } finally {
                    graphics.dispose();
                }
                // WebP has a reader only; save resized WebP images as PNG to preserve transparency.
                String format = jpeg ? "jpeg" : "png";
                return image.withEncoded(
                        "profile." + format,
                        "image/" + format,
                        jpeg ? ImageFormat.JPEG : ImageFormat.PNG,
                        encode(resized, format));
            } finally {
                reader.dispose();
            }
        } catch (IOException e) {
            throw new NotValidImageContentException("Unable to process profile image. Please upload a valid image.", e);
        }
    }

    private static void requireProfileDimensions(int width, int height) {
        if (width < PROFILE_IMAGE_DIMENSION || height < PROFILE_IMAGE_DIMENSION) {
            throw new InvalidImageSizeException("Profile image must be at least 400x400 pixels.");
        }
    }

    private static BufferedImage centerCropAndScale(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        int cropSize = Math.min(width, height);
        BufferedImage crop = source.getSubimage((width - cropSize) / 2, (height - cropSize) / 2, cropSize, cropSize);
        BufferedImage resized = new BufferedImage(PROFILE_IMAGE_DIMENSION, PROFILE_IMAGE_DIMENSION,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = resized.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.drawImage(crop, 0, 0, PROFILE_IMAGE_DIMENSION, PROFILE_IMAGE_DIMENSION, null);
            return resized;
        } finally {
            graphics.dispose();
        }
    }

    private static byte[] encode(BufferedImage image, String format) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            if (!ImageIO.write(image, format, output)) {
                throw new IOException("Unable to encode resized profile image.");
            }
            return output.toByteArray();
        } catch (IOException e) {
            throw new NotValidImageContentException("Unable to process profile image. Please upload a valid image.", e);
        }
    }

    private static InputStream open(ImagePayload image) throws IOException {
        if (image.content() != null) {
            return new ByteArrayInputStream(image.content());
        }
        return image.source().getInputStream();
    }
}
