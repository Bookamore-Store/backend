package com.bookamore.backend.controller;

import com.bookamore.backend.dto.chat.ChatInboxItemResponse;
import com.bookamore.backend.dto.chat.ChatMessageListResponse;
import com.bookamore.backend.dto.chat.ChatMessageRequest;
import com.bookamore.backend.dto.chat.ChatMessageResponse;
import com.bookamore.backend.dto.chat.ChatReadResponse;
import com.bookamore.backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

    @GetMapping
    public Page<ChatInboxItemResponse> listInbox(@RequestParam(defaultValue = "0") Integer page,
                                                 @RequestParam(defaultValue = "20") Integer size) {
        return chatService.listInbox(page, size);
    }

    @GetMapping("/{conversationId}/messages")
    public ChatMessageListResponse listMessages(@PathVariable UUID conversationId,
                                                @RequestParam(required = false) UUID after,
                                                @RequestParam(required = false) UUID before,
                                                @RequestParam(required = false) Integer limit) {
        return chatService.listMessages(conversationId, after, before, limit);
    }

    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<ChatMessageResponse> sendMessage(@PathVariable UUID conversationId,
                                                           @Validated @RequestBody ChatMessageRequest request) {
        return ResponseEntity.ok(chatService.sendMessage(conversationId, request));
    }

    @PostMapping("/{conversationId}/read")
    public ResponseEntity<ChatReadResponse> markAsRead(@PathVariable UUID conversationId,
                                                       @RequestParam(required = false) UUID upToMessageId) {
        return ResponseEntity.ok(chatService.markAsRead(conversationId, upToMessageId));
    }
}
