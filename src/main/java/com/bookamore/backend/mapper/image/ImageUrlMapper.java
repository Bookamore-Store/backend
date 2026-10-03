package com.bookamore.backend.mapper.image;

import com.bookamore.backend.service.ImageStorageRegistry;
import com.bookamore.backend.service.ImageStorageType;
import com.bookamore.backend.service.impl.S3ImageUrlService;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ImageUrlMapper {

    private final ImageStorageRegistry storageRegistry;
    private final ObjectProvider<S3ImageUrlService> s3ImageUrlService;

    @Named("imageUrl")
    public String toClientUrl(String path) {
        if (storageRegistry.getWriteTarget() != ImageStorageType.S3) {
            return path;
        }
        return s3ImageUrlService.getObject().toPublicUrl(path);
    }
}
