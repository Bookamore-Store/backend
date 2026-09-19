package com.bookamore.backend.config;

import com.bookamore.backend.service.ImageStorageRegistry;
import com.bookamore.backend.repository.ImageStorageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Slf4j
@Configuration
public class ImageStorageRoutingConfig {

    static final String LOCAL_BEAN = "localImageStorageRepository";
    static final String S3_BEAN = "s3ImageStorageRepository";

    @Bean
    public ImageStorageRegistry imageStorageRegistry(
            FileConfig fileConfig,
            @Autowired(required = false) @Qualifier(LOCAL_BEAN) ImageStorageRepository localStorage,
            @Autowired(required = false) @Qualifier(S3_BEAN) ImageStorageRepository s3Storage
    ) {
        ImageStorageRegistry registry = ImageStorageRegistry.of(
                fileConfig.getStorage().getWrite(),
                localStorage,
                s3Storage
        );
        log.info("Active image storage={}, available={}", registry.getWriteTarget(), registry.getAvailable().keySet());
        return registry;
    }

    @Bean
    @Primary
    public ImageStorageRepository imageStorageRepository(ImageStorageRegistry registry) {
        return registry.writeStorage();
    }
}
