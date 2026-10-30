package com.bookamore.backend.service.imageprocessing.decoder;

import com.bookamore.backend.exception.NotValidImageContentException;
import openize.heic.decoder.HeicImage;
import openize.heic.decoder.HeicImageFrame;
import openize.heic.decoder.PixelFormat;
import openize.io.IOSeekMode;
import openize.io.IOStream;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;

@Component
public class HeicDecoder {

    /**
     * Decodes a HEIC/HEIF file.
     * Openize applies the container rotation and mirror (irot/imir) while decoding.
     */
    public BufferedImage decode(byte[] heicBytes) {
        try {
            ByteArrayIOStream input = new ByteArrayIOStream(heicBytes);
            HeicImage heic = HeicImage.load(input);
            int width = Math.toIntExact(heic.getWidth());
            int height = Math.toIntExact(heic.getHeight());
            if (width <= 0 || height <= 0) {
                throw new NotValidImageContentException("Unable to read HEIC image. Please upload a valid image.");
            }
            HeicImageFrame frame = heic.getDefaultFrame();
            boolean alpha = frame != null && frame.hasAlpha();
            int[] pixels = heic.getInt32Array(PixelFormat.Argb32);
            long pixelCount = width * (long) height;
            if (pixels == null || pixelCount <= 0 || pixelCount >= Integer.MAX_VALUE || pixelCount > pixels.length) {
                throw new NotValidImageContentException("Unable to read HEIC image. Please upload a valid image.");
            }
            if (!alpha) {
                int count = (int) pixelCount;
                for (int i = 0; i < count; i++) {
                    pixels[i] |= 0xFF000000;
                }
            }
            BufferedImage image = new BufferedImage(width, height,
                    alpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
            image.setRGB(0, 0, width, height, pixels, 0, width);
            return image;
        } catch (NotValidImageContentException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new NotValidImageContentException("Unable to read HEIC image. Please upload a valid image.", e);
        }
    }

    /**
     * In-memory {@link IOStream} so HEIC bytes are decoded without a temporary file.
     */
    private static final class ByteArrayIOStream implements IOStream {
        private final byte[] data;
        private int position;

        private ByteArrayIOStream(byte[] data) {
            this.data = data;
        }

        @Override
        public int read(byte[] dst) {
            return read(dst, 0, dst.length);
        }

        @Override
        public int read(byte[] dst, int offset, int count) {
            if (position >= data.length) {
                return -1;
            }
            int readable = Math.min(count, data.length - position);
            System.arraycopy(data, position, dst, offset, readable);
            position += readable;
            return readable;
        }

        @Override
        public void write(byte[] data) {
            throw new openize.io.IOException("HEIC input is read-only.");
        }

        @Override
        public void write(byte[] data, int offset, int count) {
            throw new openize.io.IOException("HEIC input is read-only.");
        }

        @Override
        public long setPosition(long newPosition) {
            long previous = position;
            if (newPosition < 0 || newPosition > data.length) {
                throw new openize.io.IOException("Position out of range: " + newPosition);
            }
            position = (int) newPosition;
            return previous;
        }

        @Override
        public long getPosition() {
            return position;
        }

        @Override
        public void seek(long newPosition, IOSeekMode mode) {
            long target = switch (mode) {
                case BEGIN -> newPosition;
                case CURRENT -> position + newPosition;
                case END -> data.length + newPosition;
            };
            setPosition(target);
        }

        @Override
        public long getLength() {
            return data.length;
        }

        @Override
        public void setLength(long newLength) {
            throw new openize.io.IOException("HEIC input is read-only.");
        }

        @Override
        public void close() {
        }
    }
}
