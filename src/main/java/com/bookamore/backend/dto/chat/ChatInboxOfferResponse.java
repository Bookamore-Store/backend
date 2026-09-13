package com.bookamore.backend.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatInboxOfferResponse {
    private UUID id;
    private BigDecimal price;
    private String bookTitle;
}
