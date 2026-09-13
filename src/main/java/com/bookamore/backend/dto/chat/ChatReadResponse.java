package com.bookamore.backend.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatReadResponse {
    @Schema(example = "d367ec63-a73b-485d-a1c8-6b793b009e53", description = "Conversation id")
    private UUID conversationId;

    @Schema(example = "0", description = "Remaining unread count for the current user")
    private int unreadCount;
}
