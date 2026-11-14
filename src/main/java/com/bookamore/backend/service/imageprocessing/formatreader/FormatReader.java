package com.bookamore.backend.service.imageprocessing.formatreader;

import com.bookamore.backend.service.imageprocessing.ImageFormat;
import com.bookamore.backend.service.imageprocessing.ImagePayload;

/**
 * Turns one input format into the form the rest of the pipeline can store.
 * Register a new {@code @Component} to support another format.
 */
public interface FormatReader {

    boolean supports(ImageFormat format);

    ImagePayload read(ImagePayload image);
}
