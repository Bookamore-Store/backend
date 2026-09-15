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
public class ChatConversationDetailResponse {
    @Schema(example = "d367ec63-a73b-485d-a1c8-6b793b009e53", description = "Conversation id")
    private UUID id;

    @Schema(description = "Offer this conversation belongs to")
    private ChatOfferDetailResponse offer;

    @Schema(description = "Seller participant",
            example = """
                    {"id":"018d4f1a-5b03-71d4-a716-446655440001","name":"John","avatarUrl":"https://cdn.example.com/avatars/john.png","role":"SELLER"}
                    """)
    private ChatParticipantResponse seller;

    @Schema(description = "Buyer participant",
            example = """
                    {"id":"018d4f1a-5b03-71d4-a716-446655440002","name":"Jane","avatarUrl":"https://cdn.example.com/avatars/jane.png","role":"BUYER"}
                    """)
    private ChatParticipantResponse buyer;

    @Schema(example = "2026-03-20T14:30:00", description = "Timestamp of the last message")
    private LocalDateTime lastMessageAt;

    @Schema(example = "Hi, is this book still available?", description = "Preview of the last message")
    private String lastMessagePreview;

    @Schema(example = "2", description = "Unread message count for the current user")
    private int unreadCount;
}
