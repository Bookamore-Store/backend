package com.bookamore.backend.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatConversationDetailResponse {
    private UUID id;
    private ChatOfferDetailResponse offer;
    private ChatParticipantResponse seller;
    private ChatParticipantResponse buyer;
    private LocalDateTime lastMessageAt;
    private String lastMessagePreview;
    private int unreadCount;
}
