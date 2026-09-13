package com.bookamore.backend.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatInboxOfferResponse {
    @Schema(example = "d367ec63-a73b-485d-a1c8-6b793b009e53", description = "Offer id")
    private UUID id;

    @Schema(example = "19.99", description = "Price of the book")
    private BigDecimal price;

    @Schema(example = "Clean Code", description = "Book title")
    private String bookTitle;
}
