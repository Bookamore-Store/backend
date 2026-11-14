package com.bookamore.backend.service.imageprocessing.policy;

import com.bookamore.backend.entity.enums.EntityType;
import com.bookamore.backend.service.imageprocessing.ImagePayload;

/**
 * Entity-specific preparation of an already normalized image.
 * Register a new {@code @Component} to add processing for another entity type.
 * An entity type without a policy is stored as normalized.
 */
public interface EntityImagePolicy {

    EntityType entityType();

    ImagePayload apply(ImagePayload image);
}
