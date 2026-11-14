package com.bookamore.backend.repository;

import com.bookamore.backend.entity.Image;
import com.bookamore.backend.entity.enums.EntityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ImageRepository extends JpaRepository<Image, UUID> {
    long countByEntityTypeAndEntityId(EntityType entityType, UUID entityId);

    List<Image> findAllByEntityTypeAndEntityId(EntityType entityType, UUID entityId);

    @Query("select coalesce(max(i.orderIndex), -1) from Image i where i.entityType = :entityType and i.entityId = :entityId")
    int findMaxOrderIndexByEntityTypeAndEntityId(@Param("entityType") EntityType entityType,
                                                  @Param("entityId") UUID entityId);
}
