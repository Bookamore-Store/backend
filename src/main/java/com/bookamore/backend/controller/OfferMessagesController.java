package com.bookamore.backend.controller;

import com.bookamore.backend.dto.chat.ChatMessageListQuery;
import com.bookamore.backend.dto.chat.ChatMessageListResponse;
import com.bookamore.backend.dto.chat.ChatMessageRequest;
import com.bookamore.backend.dto.chat.ChatMessageResponse;
import com.bookamore.backend.dto.chat.ChatReadRequest;
import com.bookamore.backend.dto.chat.ChatReadResponse;
import com.bookamore.backend.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/offers")
@RequiredArgsConstructor
public class OfferMessagesController {

    private final ChatService chatService;

    @Operation(summary = "Get conversation messages for offer",
            description = "Same as GET /api/v1/conversations/{conversationId}/messages. "
                    + "The conversation is resolved from the offer and the authenticated user (buyer). "
                    + "If none exists, returns an empty message list without creating a conversation. "
                    + "`after` and `before` cannot be used together. Requires JWT.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Message list",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ChatMessageListResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "`after` and `before` cannot be used together"),
            @ApiResponse(responseCode = "404", description = "Offer not found"),
            @ApiResponse(responseCode = "422",
                    description = "Seller cannot list messages for their own offer")
    })
    @GetMapping("/{offerId}/messages")
    public ChatMessageListResponse listMessages(@PathVariable UUID offerId,
                                                @Validated @ParameterObject ChatMessageListQuery query) {
        return chatService.listMessagesForOffer(offerId, query);
    }

    @Operation(summary = "Send message for offer",
            description = "Same as POST /api/v1/conversations/{conversationId}/messages. "
                    + "The conversation is resolved from the offer and the authenticated user (buyer); "
                    + "if none exists it is created. Requires JWT.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Message sent",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ChatMessageResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Message content is blank or longer than 2000 characters"),
            @ApiResponse(responseCode = "404", description = "Offer not found"),
            @ApiResponse(responseCode = "422",
                    description = "Seller cannot start a conversation with themselves or offer is not OPEN")
    })
    @PostMapping("/{offerId}/messages")
    public ResponseEntity<ChatMessageResponse> sendMessage(@PathVariable UUID offerId,
                                                           @Validated @RequestBody ChatMessageRequest request) {
        return ResponseEntity.ok(chatService.sendMessageForOffer(offerId, request));
    }

    @Operation(summary = "Mark conversation as read for offer",
            description = "Same as POST /api/v1/conversations/{conversationId}/read. "
                    + "The conversation is resolved from the offer and the authenticated user (buyer). "
                    + "Marks counterpart messages as read up to `upToMessageId` in the body, "
                    + "or up to the latest message if the body is omitted or the field is null. Requires JWT.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Read watermark updated",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ChatReadResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Offer, conversation or message not found")
    })
    @PostMapping("/{offerId}/messages/read")
    public ResponseEntity<ChatReadResponse> markAsRead(@PathVariable UUID offerId,
                                                       @RequestBody(required = false) ChatReadRequest request) {
        UUID upToMessageId = request != null ? request.getUpToMessageId() : null;
        return ResponseEntity.ok(chatService.markAsReadForOffer(offerId, upToMessageId));
    }
}
