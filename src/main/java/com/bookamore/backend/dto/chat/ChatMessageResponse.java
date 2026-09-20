package com.bookamore.backend.dto.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    @Schema(example = "d367ec63-a73b-485d-a1c8-6b793b009e53", description = "Message id")
    private UUID id;

    @Schema(example = "d367ec63-a73b-485d-a1c8-6b793b009e53", description = "Sender user id")
    private UUID senderId;

    @Schema(example = "Sounds great! What time works for you?", description = "Message text")
    private String content;

    @JsonProperty("isRead")
    @Schema(example = "false", description = "Whether the counterpart has read the message")
    private boolean read;

    @Schema(example = "2026-03-20T14:30:00", description = "When the message was sent")
    private LocalDateTime createdDate;
}
