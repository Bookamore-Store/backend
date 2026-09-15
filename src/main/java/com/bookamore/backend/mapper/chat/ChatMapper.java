package com.bookamore.backend.mapper.chat;

import com.bookamore.backend.dto.chat.ChatBookSummaryResponse;
import com.bookamore.backend.dto.chat.ChatInboxItemResponse;
import com.bookamore.backend.dto.chat.ChatMessageResponse;
import com.bookamore.backend.dto.chat.ChatOfferDetailResponse;
import com.bookamore.backend.dto.chat.ChatParticipantResponse;
import com.bookamore.backend.entity.Book;
import com.bookamore.backend.entity.ChatConversation;
import com.bookamore.backend.entity.ChatMessage;
import com.bookamore.backend.entity.Offer;
import com.bookamore.backend.entity.User;
import com.bookamore.backend.dto.chat.ChatParticipantRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ChatMapper {

    ChatBookSummaryResponse toBookSummary(Book book);

    ChatOfferDetailResponse toOfferDetail(Offer offer);

    @Mapping(target = "role", ignore = true)
    ChatParticipantResponse toParticipant(User user);

    @Mapping(target = "counterpart", ignore = true)
    @Mapping(target = "unreadCount", ignore = true)
    ChatInboxItemResponse toInboxItemBase(ChatConversation conversation);

    @Mapping(target = "senderId", source = "sender.id")
    @Mapping(target = "conversationId", source = "conversation.id")
    ChatMessageResponse toMessage(ChatMessage message);

    List<ChatMessageResponse> toMessages(List<ChatMessage> messages);

    default ChatInboxItemResponse toInboxItem(ChatConversation conversation, UUID currentUserId) {
        ChatInboxItemResponse response = toInboxItemBase(conversation);
        boolean buyer = conversation.getBuyer().getId().equals(currentUserId);
        User counterpart = buyer ? conversation.getSeller() : conversation.getBuyer();
        ChatParticipantResponse participant = toParticipant(counterpart);
        participant.setRole(buyer ? ChatParticipantRole.SELLER : ChatParticipantRole.BUYER);
        response.setCounterpart(participant);
        response.setUnreadCount(unreadCount(conversation, currentUserId));
        return response;
    }

    default int unreadCount(ChatConversation conversation, UUID currentUserId) {
        if (conversation.getBuyer().getId().equals(currentUserId)) {
            return conversation.getBuyerUnreadCount();
        }
        return conversation.getSellerUnreadCount();
    }
}
