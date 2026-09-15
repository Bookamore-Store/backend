package com.bookamore.backend.dto.chat;

import com.bookamore.backend.entity.enums.OfferStatus;
import com.bookamore.backend.entity.enums.OfferType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatOfferDetailResponse {
    @Schema(example = "d367ec63-a73b-485d-a1c8-6b793b009e53", description = "Offer id")
    private UUID id;

    @Schema(example = "19.99", description = "Price of the book")
    private BigDecimal price;

    @Schema(example = "OPEN", description = "Status of the offer")
    private OfferStatus status;

    @Schema(example = "SELL", description = "Type of the offer")
    private OfferType type;

    @Schema(description = "Book summary")
    private ChatBookSummaryResponse book;
}
