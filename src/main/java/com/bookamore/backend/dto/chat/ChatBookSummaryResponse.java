package com.bookamore.backend.dto.chat;

import com.bookamore.backend.entity.enums.BookCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatBookSummaryResponse {
    @Schema(example = "d367ec63-a73b-485d-a1c8-6b793b009e53", description = "Book id")
    private UUID id;

    @Schema(example = "Clean Code", description = "Book title")
    private String title;

    @Schema(example = "NEW", description = "Condition of the book")
    private BookCondition condition;
}
