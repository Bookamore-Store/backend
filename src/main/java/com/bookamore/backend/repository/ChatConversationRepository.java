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
            value = """
                    SELECT c FROM ChatConversation c
                    WHERE (c.buyer.id = :userId AND :includeBuyer = true)
                       OR (c.seller.id = :userId AND :includeSeller = true)
                    ORDER BY
                      CASE WHEN (c.buyer.id = :userId AND c.buyerUnreadCount > 0)
                             OR (c.seller.id = :userId AND c.sellerUnreadCount > 0)
                           THEN 0 ELSE 1 END,
                      c.lastMessageAt DESC NULLS LAST
                    """,
            countQuery = """
                    SELECT COUNT(c) FROM ChatConversation c
                    WHERE (c.buyer.id = :userId AND :includeBuyer = true)
                       OR (c.seller.id = :userId AND :includeSeller = true)
                    """
    )
    Page<ChatConversation> findInboxByUserId(@Param("userId") UUID userId,
                                             @Param("includeBuyer") boolean includeBuyer,
                                             @Param("includeSeller") boolean includeSeller,
                                             Pageable pageable);

    @Query("SELECT c FROM ChatConversation c WHERE c.id = :id AND (c.buyer.id = :userId OR c.seller.id = :userId)")
    Optional<ChatConversation> findByIdAndParticipant(@Param("id") UUID id, @Param("userId") UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ChatConversation c WHERE c.id = :id AND (c.buyer.id = :userId OR c.seller.id = :userId)")
    Optional<ChatConversation> findByIdAndParticipantForUpdate(@Param("id") UUID id, @Param("userId") UUID userId);

    @Query("""
            SELECT COALESCE(SUM(CASE WHEN c.buyer.id = :userId THEN c.buyerUnreadCount ELSE c.sellerUnreadCount END), 0) AS unreadMessages,
                   COALESCE(SUM(CASE WHEN (CASE WHEN c.buyer.id = :userId THEN c.buyerUnreadCount ELSE c.sellerUnreadCount END) > 0 THEN 1 ELSE 0 END), 0) AS unreadConversations
            FROM ChatConversation c
            WHERE (c.buyer.id = :userId AND :includeBuyer = true)
               OR (c.seller.id = :userId AND :includeSeller = true)
            """)
    UnreadCountAggregate aggregateUnreadByUserId(@Param("userId") UUID userId,
                                                 @Param("includeBuyer") boolean includeBuyer,
                                                 @Param("includeSeller") boolean includeSeller);

    interface UnreadCountAggregate {
        long getUnreadMessages();

        long getUnreadConversations();
    }
}
