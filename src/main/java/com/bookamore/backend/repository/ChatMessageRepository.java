package com.bookamore.backend.repository;

import com.bookamore.backend.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    Optional<ChatMessage> findByIdAndConversationId(UUID id, UUID conversationId);

    List<ChatMessage> findByConversationIdAndIdGreaterThanOrderByIdAsc(UUID conversationId,
                                                                       UUID after,
                                                                       Pageable pageable);

    List<ChatMessage> findByConversationIdAndIdLessThanOrderByIdDesc(UUID conversationId,
                                                                     UUID before,
                                                                     Pageable pageable);

    List<ChatMessage> findByConversationIdOrderByIdDesc(UUID conversationId, Pageable pageable);

    @Query("SELECT MAX(m.id) FROM ChatMessage m WHERE m.conversation.id = :conversationId")
    UUID findMaxIdByConversationId(@Param("conversationId") UUID conversationId);

    @Modifying(flushAutomatically = true)
    @Query(value = """
            UPDATE chat_messages
            SET is_read = true
            WHERE conversation_id = :conversationId
              AND sender_id <> :readerId
              AND id <= :upToMessageId
              AND is_read = false
            """, nativeQuery = true)
    void markCounterpartMessagesReadUpTo(@Param("conversationId") UUID conversationId,
                                        @Param("readerId") UUID readerId,
                                        @Param("upToMessageId") UUID upToMessageId);

    @Query(value = """
            SELECT COUNT(*) FROM chat_messages
            WHERE conversation_id = :conversationId
              AND sender_id <> :readerId
              AND is_read = false
            """, nativeQuery = true)
    long countUnreadFromCounterpart(@Param("conversationId") UUID conversationId,
                                    @Param("readerId") UUID readerId);
}
