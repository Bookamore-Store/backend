package com.bookamore.backend.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatUnreadCountResponse {
    @Schema(example = "3", description = "Sum of unread messages across all conversations for the current user")
    private int unreadMessages;

    @Schema(example = "2", description = "Number of conversations with at least one unread message")
    private int unreadConversations;
}
