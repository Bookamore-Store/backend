package com.bookamore.backend.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageListResponse {
    private UUID conversationId;
    private List<ChatMessageResponse> messages;
}
