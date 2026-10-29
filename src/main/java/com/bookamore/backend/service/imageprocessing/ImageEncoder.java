package com.bookamore.backend.service.imageprocessing;

import com.bookamore.backend.exception.NotValidImageContentException;
import com.luciad.imageio.webp.CompressionType;
import com.luciad.imageio.webp.WebPWriteParam;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Iterator;

@Component
public class ImageEncoder {

    public MultipartFile toMultipartFile(ImagePayload payload) {
        if (payload.untouched()) {
            return payload.source();
        }
        if (payload.content() != null) {
            return new InMemoryMultipartFile(payload.fileName(), payload.contentType(), payload.content());
        }
        BufferedImage decoded = payload.decoded();
        if (decoded == null || payload.format() != ImageFormat.WEBP) {
            throw new NotValidImageContentException("Unable to encode image.");
        }
        return new InMemoryMultipartFile(payload.fileName(), payload.contentType(), encodeWebp(decoded));
    }

    private static byte[] encodeWebp(BufferedImage image) {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType("image/webp");
        if (!writers.hasNext()) {
            throw new NotValidImageContentException("Unable to encode image as WebP.");
        }
        ImageWriter writer = writers.next();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ImageOutputStream output = new MemoryCacheImageOutputStream(bytes)) {
            writer.setOutput(output);
            WebPWriteParam param = new WebPWriteParam(writer.getLocale());
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionType(CompressionType.Lossy);
            writer.write(null, new IIOImage(image, null, null), param);
            return bytes.toByteArray();
        } catch (IOException e) {
            throw new NotValidImageContentException("Unable to encode image as WebP.", e);
        } finally {
            writer.dispose();
        }
    }

    private record InMemoryMultipartFile(String fileName, String contentType, byte[] content) implements MultipartFile {
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
