package com.bookamore.backend.exception;

import com.bookamore.backend.entity.enums.EntityType;

public class UnsupportedEntityTypeException extends RuntimeException {
    public UnsupportedEntityTypeException(EntityType entityType) {
        super("Unsupported entity type: " + entityType);
    }
}
