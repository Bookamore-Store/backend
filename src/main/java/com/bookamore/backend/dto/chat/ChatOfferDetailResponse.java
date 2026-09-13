package com.bookamore.backend.dto.chat;

import com.bookamore.backend.entity.enums.OfferStatus;
import com.bookamore.backend.entity.enums.OfferType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatOfferDetailResponse {
    private UUID id;
    private BigDecimal price;
    private OfferStatus status;
    private OfferType type;
    private ChatBookSummaryResponse book;
}
