package com.bookamore.backend.service;

import com.bookamore.backend.dto.image.ImageTransferRequest;
import com.bookamore.backend.dto.image.ImageTransferResponse;

public interface ImageTransferService {

    ImageTransferResponse transfer(ImageTransferRequest request);
}
