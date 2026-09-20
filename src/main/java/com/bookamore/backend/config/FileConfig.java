package com.bookamore.backend.config;

import com.bookamore.backend.service.ImageStorageType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@ToString
@Configuration
@ConfigurationProperties(prefix = "file")
public class FileConfig {

    private String uploadDir;

    private String subDirsPerms;

    private Storage storage = new Storage();

    @Getter
    @Setter
    public static class Storage {
        /**
         * Destination for new uploads and deletes. Must be one of the enabled storages.
         */
        private ImageStorageType write = ImageStorageType.LOCAL;

        private boolean localEnabled = true;

        private boolean s3Enabled = false;
    }
}
