package com.bookamore.backend.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChatStartRequest {

    @NotBlank(message = "Initial message cannot be blank")
    @Size(max = 2000, message = "Initial message must be at most 2000 characters")
    @Schema(example = "Hi, is this book still available?",
            description = "Opening message from the buyer. Required. On first create it starts the conversation; "
                    + "if the conversation already exists it is appended as a new message.")
    private String initialMessage;
}
