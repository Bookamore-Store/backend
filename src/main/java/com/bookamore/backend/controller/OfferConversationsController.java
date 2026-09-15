package com.bookamore.backend.controller;

import com.bookamore.backend.dto.chat.ChatInboxItemResponse;
import com.bookamore.backend.dto.chat.ChatStartRequest;
import com.bookamore.backend.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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

    @Operation(summary = "Get or create conversation for offer",
            description = "Returns the existing conversation for the authenticated buyer and offer, or creates one. "
                    + "initialMessage is required. If the conversation already exists, the message is appended "
                    + "as a new buyer message (same as sending to the thread). Always 200 (get-or-create, not 201). "
                    + "Seller cannot start a chat with themselves. Requires JWT.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Conversation found or created",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ChatInboxItemResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400",
                    description = "Initial message is blank or longer than 2000 characters"),
            @ApiResponse(responseCode = "404", description = "Offer not found"),
            @ApiResponse(responseCode = "422",
                    description = "Seller cannot start a conversation with themselves or offer is not OPEN")
    })
    @PostMapping("/{offerId}/conversations")
    public ResponseEntity<ChatInboxItemResponse> getOrCreate(
            @PathVariable UUID offerId,
            @Validated @RequestBody ChatStartRequest request) {
        return ResponseEntity.ok(chatService.getOrCreateForOffer(offerId, request));
    }
}
