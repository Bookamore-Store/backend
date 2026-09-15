package com.bookamore.backend.service;

import com.bookamore.backend.dto.chat.ChatInboxItemResponse;
import com.bookamore.backend.dto.chat.ChatMessageListQuery;
import com.bookamore.backend.dto.chat.ChatMessageListResponse;
import com.bookamore.backend.dto.chat.ChatMessageRequest;
import com.bookamore.backend.dto.chat.ChatMessageResponse;
import com.bookamore.backend.dto.chat.ChatReadResponse;
import com.bookamore.backend.dto.chat.ChatStartRequest;
import com.bookamore.backend.dto.chat.ChatUnreadCountResponse;
import com.bookamore.backend.dto.chat.ChatParticipantRole;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface ChatService {

    ChatInboxItemResponse getOrCreateForOffer(UUID offerId, ChatStartRequest request);

    Page<ChatInboxItemResponse> listInbox(Integer page, Integer size, ChatParticipantRole role);

    ChatUnreadCountResponse getUnreadCount(ChatParticipantRole role);

    ChatMessageListResponse listMessages(UUID conversationId, ChatMessageListQuery query);

    ChatMessageResponse sendMessage(UUID conversationId, ChatMessageRequest request);

    ChatReadResponse markAsRead(UUID conversationId, UUID upToMessageId);
}
