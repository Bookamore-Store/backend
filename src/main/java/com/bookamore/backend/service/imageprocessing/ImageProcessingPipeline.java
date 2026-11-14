package com.bookamore.backend.service.imageprocessing;

import com.bookamore.backend.entity.enums.EntityType;
import com.bookamore.backend.service.imageprocessing.formatreader.FormatReader;
import com.bookamore.backend.service.imageprocessing.policy.EntityImagePolicy;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Prepares an uploaded image for storage.
 * Format normalizers run first; the first one that supports the input format is used.
 * The entity policy, when one is registered, runs after normalization.
 * An entity type without a policy keeps the normalized image.
 */
@Component
public class ImageProcessingPipeline {

    private final List<FormatReader> normalizers;
    private final Map<EntityType, EntityImagePolicy> policies;
    private final ImageEncoder encoder;

    public ImageProcessingPipeline(List<FormatReader> normalizers,
                                   List<EntityImagePolicy> policies,
                                   ImageEncoder encoder) {
        this.normalizers = List.copyOf(normalizers);
        this.policies = index(policies);
        this.encoder = encoder;
    }

    public MultipartFile process(MultipartFile file, EntityType entityType) {
        ImagePayload payload = ImagePayload.from(file);
        for (FormatReader normalizer : normalizers) {
            if (normalizer.supports(payload.format())) {
                payload = normalizer.read(payload);
                break;
            }
        }
        EntityImagePolicy policy = policies.get(entityType);
        if (policy != null) {
            payload = policy.apply(payload);
        }
        return encoder.toMultipartFile(payload);
    }

    private static Map<EntityType, EntityImagePolicy> index(List<EntityImagePolicy> policies) {
        Map<EntityType, EntityImagePolicy> indexed = new EnumMap<>(EntityType.class);
        for (EntityImagePolicy policy : policies) {
            EntityImagePolicy previous = indexed.put(policy.entityType(), policy);
            if (previous != null) {
                throw new IllegalStateException("Duplicate image policy for " + policy.entityType());
            }
        }
        return Map.copyOf(indexed);
    }
}
