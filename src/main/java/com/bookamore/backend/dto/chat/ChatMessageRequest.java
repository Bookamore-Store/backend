package com.bookamore.backend.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChatMessageRequest {

    @NotBlank(message = "Message content cannot be blank")
    @Size(max = 2000, message = "Message content must be at most 2000 characters")
    @Schema(example = "Sounds great! What time works for you?", description = "Message text")
    private String content;
}
