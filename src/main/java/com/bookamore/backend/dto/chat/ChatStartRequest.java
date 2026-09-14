package com.bookamore.backend.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChatStartRequest {

    @Size(max = 2000, message = "Initial message must be at most 2000 characters")
    @Schema(example = "Hi, is this book still available?",
            description = "Optional message from the buyer. On first create it is the opening message; "
                    + "if the conversation already exists it is appended as a new message.")
    private String initialMessage;
}
