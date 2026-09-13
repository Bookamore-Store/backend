package com.bookamore.backend.controller;

import com.bookamore.backend.dto.chat.ChatConversationDetailResponse;
import com.bookamore.backend.dto.chat.ChatStartRequest;
import com.bookamore.backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/offers")
@RequiredArgsConstructor
public class OfferConversationsController {

    private final ChatService chatService;

    @PostMapping("/{offerId}/conversations")
    public ResponseEntity<ChatConversationDetailResponse> getOrCreate(
            @PathVariable UUID offerId,
            @Validated @RequestBody(required = false) ChatStartRequest request) {
        return ResponseEntity.ok(chatService.getOrCreateForOffer(offerId, request));
    }
}
