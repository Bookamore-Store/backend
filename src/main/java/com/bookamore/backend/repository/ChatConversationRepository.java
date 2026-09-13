package com.bookamore.backend.repository;

import com.bookamore.backend.entity.ChatConversation;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ChatConversationRepository extends JpaRepository<ChatConversation, UUID> {

    @EntityGraph(attributePaths = {"offer", "offer.book", "seller", "buyer"})
    @Query("SELECT c FROM ChatConversation c WHERE c.offer.id = :offerId AND c.buyer.id = :buyerId")
    Optional<ChatConversation> findByOfferIdAndBuyerId(@Param("offerId") UUID offerId,
                                                       @Param("buyerId") UUID buyerId);

    @EntityGraph(attributePaths = {"offer", "offer.book", "seller", "buyer"})
    @Query(
            value = "SELECT c FROM ChatConversation c WHERE c.buyer.id = :userId OR c.seller.id = :userId",
            countQuery = "SELECT COUNT(c) FROM ChatConversation c WHERE c.buyer.id = :userId OR c.seller.id = :userId"
    )
    Page<ChatConversation> findInboxByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT c FROM ChatConversation c WHERE c.id = :id AND (c.buyer.id = :userId OR c.seller.id = :userId)")
    Optional<ChatConversation> findByIdAndParticipant(@Param("id") UUID id, @Param("userId") UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ChatConversation c WHERE c.id = :id AND (c.buyer.id = :userId OR c.seller.id = :userId)")
    Optional<ChatConversation> findByIdAndParticipantForUpdate(@Param("id") UUID id, @Param("userId") UUID userId);
}
