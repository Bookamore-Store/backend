package com.bookamore.backend.dto.image;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ImageTransferResponse {
    int scanned;
    int copied;
    int skipped;
    int missing;
    int failed;
}
