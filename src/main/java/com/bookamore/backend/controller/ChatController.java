package com.bookamore.backend.controller;

import com.bookamore.backend.annotation.No400Swgr;
import com.bookamore.backend.annotation.No404Swgr;
import com.bookamore.backend.dto.chat.ChatInboxItemResponse;
import com.bookamore.backend.dto.chat.ChatMessageListResponse;
import com.bookamore.backend.dto.chat.ChatMessageRequest;
import com.bookamore.backend.dto.chat.ChatMessageResponse;
import com.bookamore.backend.dto.chat.ChatReadResponse;
import com.bookamore.backend.dto.chat.ChatUnreadCountResponse;
import com.bookamore.backend.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/conversations")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @No400Swgr
    @No404Swgr
    @Operation(summary = "Get conversation inbox",
            description = "Returns a page of conversations for the authenticated user. Unread conversations come first, then newest last message. Requires JWT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inbox page")
    })
    @GetMapping
    public Page<ChatInboxItemResponse> listInbox(@RequestParam(defaultValue = "0") Integer page,
                                                 @RequestParam(defaultValue = "20") Integer size) {
        return chatService.listInbox(page, size);
    }

    @No400Swgr
    @No404Swgr
    @Operation(summary = "Get unread chat counts",
            description = "Returns the total unread message count and the number of conversations with unread messages for the authenticated user. Requires JWT.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Unread counts",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ChatUnreadCountResponse.class)
                    )
            )
    })
    @GetMapping("/unread-count")
    public ChatUnreadCountResponse getUnreadCount() {
        return chatService.getUnreadCount();
    }

    @Operation(summary = "Get conversation messages",
            description = "Returns messages in chronological order. `after` and `before` cannot be used together. Requires JWT.")
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
            @ApiResponse(responseCode = "404", description = "Conversation not found")
    })
    @GetMapping("/{conversationId}/messages")
    public ChatMessageListResponse listMessages(@PathVariable UUID conversationId,
                                                @Parameter(description = "Return messages newer than this id (cannot be used with `before`)")
                                                @RequestParam(required = false) UUID after,
                                                @Parameter(description = "Return messages older than this id (cannot be used with `after`)")
                                                @RequestParam(required = false) UUID before,
                                                @Parameter(description = "Max messages to return (default 50, max 100)")
                                                @RequestParam(required = false) Integer limit) {
        return chatService.listMessages(conversationId, after, before, limit);
    }

    @Operation(summary = "Send message",
            description = "Sends a text message in the conversation. Requires JWT.")
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
            @ApiResponse(responseCode = "404", description = "Conversation not found")
    })
    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<ChatMessageResponse> sendMessage(@PathVariable UUID conversationId,
                                                           @Validated @RequestBody ChatMessageRequest request) {
        return ResponseEntity.ok(chatService.sendMessage(conversationId, request));
    }

    @Operation(summary = "Mark conversation as read",
            description = "Marks counterpart messages as read up to `upToMessageId`, or up to the latest message if omitted. Requires JWT.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Read watermark updated",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ChatReadResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Conversation or message not found")
    })
    @PostMapping("/{conversationId}/read")
    public ResponseEntity<ChatReadResponse> markAsRead(@PathVariable UUID conversationId,
                                                       @Parameter(description = "Mark as read up to this message id (optional)")
                                                       @RequestParam(required = false) UUID upToMessageId) {
        return ResponseEntity.ok(chatService.markAsRead(conversationId, upToMessageId));
    }
}
