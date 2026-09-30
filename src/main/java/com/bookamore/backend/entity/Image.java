package com.bookamore.backend.entity;

import com.bookamore.backend.entity.base.BaseEntity;
import com.bookamore.backend.entity.enums.EntityType;
import com.bookamore.backend.listener.ImageStorageCleanupListener;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@Entity
@Table(name = "images")
@EqualsAndHashCode(callSuper = false)
@EntityListeners(ImageStorageCleanupListener.class)
public class Image extends BaseEntity {
    @Column(unique = true, nullable = false)
    private String path;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    private EntityType entityType;


    @Column(name = "entity_id")
    private UUID entityId;

    @Column(length = 500)
    private String description;
}
