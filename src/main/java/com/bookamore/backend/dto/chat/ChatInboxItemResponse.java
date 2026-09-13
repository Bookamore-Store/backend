package com.bookamore.backend.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatInboxItemResponse {
    @Schema(example = "d367ec63-a73b-485d-a1c8-6b793b009e53", description = "Conversation id")
    private UUID id;

    @Schema(description = "Offer summary")
    private ChatInboxOfferResponse offer;

    @Schema(description = "The other participant in this conversation")
    private ChatParticipantResponse counterpart;

    @Schema(example = "Hi, is this book still available?", description = "Preview of the last message")
    private String lastMessagePreview;

    @Schema(example = "2026-03-20T14:30:00", description = "Timestamp of the last message")
    private LocalDateTime lastMessageAt;

    @Schema(example = "2", description = "Unread message count for the current user")
    private int unreadCount;
}
