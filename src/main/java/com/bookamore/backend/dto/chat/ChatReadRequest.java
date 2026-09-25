package com.bookamore.backend.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
public class ChatReadRequest {

    @Schema(example = "d367ec63-a73b-485d-a1c8-6b793b009e53",
            description = "Mark counterpart messages as read up to this id. Omit to mark the whole thread.")
    private UUID upToMessageId;
}
