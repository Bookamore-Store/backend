package com.bookamore.backend.service.impl;

import com.bookamore.backend.dto.chat.ChatConversationDetailResponse;
import com.bookamore.backend.dto.chat.ChatInboxItemResponse;
import com.bookamore.backend.dto.chat.ChatMessageListResponse;
import com.bookamore.backend.dto.chat.ChatMessageRequest;
import com.bookamore.backend.dto.chat.ChatMessageResponse;
import com.bookamore.backend.dto.chat.ChatReadResponse;
import com.bookamore.backend.dto.chat.ChatStartRequest;
import com.bookamore.backend.dto.chat.ChatUnreadCountResponse;
import com.bookamore.backend.entity.ChatConversation;
import com.bookamore.backend.entity.ChatMessage;
import com.bookamore.backend.entity.Offer;
import com.bookamore.backend.entity.User;
import com.bookamore.backend.dto.chat.ChatParticipantRole;
import com.bookamore.backend.entity.enums.OfferStatus;
import com.bookamore.backend.exception.BadRequestException;
import com.bookamore.backend.exception.ResourceNotFoundException;
import com.bookamore.backend.mapper.chat.ChatMapper;
import com.bookamore.backend.repository.ChatConversationRepository;
import com.bookamore.backend.repository.ChatMessageRepository;
import com.bookamore.backend.repository.OfferRepository;
import com.bookamore.backend.repository.UserRepository;
import com.bookamore.backend.service.ChatService;
import com.bookamore.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private static final int DEFAULT_INBOX_SIZE = 20;
    private static final int MAX_INBOX_SIZE = 50;
    private static final int DEFAULT_MESSAGE_LIMIT = 50;
    private static final int MAX_MESSAGE_LIMIT = 100;
    private static final int PREVIEW_MAX_LENGTH = 255;
    private static final int CONTENT_MAX_LENGTH = 2000;

    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final OfferRepository offerRepository;
    private final UserRepository userRepository;
    private final ChatMapper chatMapper;
    private final PlatformTransactionManager transactionManager;

    @Override
    public ChatConversationDetailResponse getOrCreateForOffer(UUID offerId, ChatStartRequest request) {
        UUID userId = SecurityUtils.requireAuthenticatedUserId();
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found with id: " + offerId));

        if (offer.getUser().getId().equals(userId)) {
            throw new BadRequestException("Seller cannot start a conversation with themselves");
        }

        String initialMessage = request != null ? request.getInitialMessage() : null;
        if (initialMessage != null) {
            if (!StringUtils.hasText(initialMessage)) {
                throw new BadRequestException("Initial message cannot be blank");
            }
            validateContentLength(initialMessage);
        }

        Optional<ChatConversation> existing = conversationRepository.findByOfferIdAndBuyerId(offerId, userId);
        if (existing.isPresent()) {
            appendInitialMessageIfPresent(existing.get().getId(), userId, initialMessage);
            return toDetailForOffer(offerId, userId);
        }

        if (offer.getStatus() != OfferStatus.OPEN) {
            throw new BadRequestException("Cannot start a conversation on a non-OPEN offer");
        }

        User buyer = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found User with uuid = " + userId));

        try {
            new TransactionTemplate(transactionManager).executeWithoutResult(status ->
                    persistNewConversation(offer, buyer, initialMessage)
            );
        } catch (DataIntegrityViolationException ex) {
            log.debug("Conversation already exists for offer {} and buyer {}", offerId, userId);
            conversationRepository.findByOfferIdAndBuyerId(offerId, userId)
                    .ifPresent(conversation ->
                            appendInitialMessageIfPresent(conversation.getId(), userId, initialMessage));
        }

        return toDetailForOffer(offerId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatInboxItemResponse> listInbox(Integer page, Integer size, ChatParticipantRole role) {
        UUID userId = SecurityUtils.requireAuthenticatedUserId();
        Pageable pageable = PageRequest.of(
                normalizePage(page),
                capLimit(size, DEFAULT_INBOX_SIZE, MAX_INBOX_SIZE)
        );
        return conversationRepository.findInboxByUserId(userId, includeBuyer(role), includeSeller(role), pageable)
                .map(conversation -> chatMapper.toInboxItem(conversation, userId));
    }

    @Override
    @Transactional(readOnly = true)
    public ChatUnreadCountResponse getUnreadCount(ChatParticipantRole role) {
        UUID userId = SecurityUtils.requireAuthenticatedUserId();
        ChatConversationRepository.UnreadCountAggregate counts =
                conversationRepository.aggregateUnreadByUserId(userId, includeBuyer(role), includeSeller(role));
        return new ChatUnreadCountResponse(
                (int) counts.getUnreadMessages(),
                (int) counts.getUnreadConversations()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ChatMessageListResponse listMessages(UUID conversationId, UUID after, UUID before, Integer limit) {
        UUID userId = SecurityUtils.requireAuthenticatedUserId();
        if (after != null && before != null) {
            throw new BadRequestException("after and before cannot be used together");
        }

        conversationRepository.findByIdAndParticipant(conversationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with id: " + conversationId));

        int cappedLimit = capLimit(limit, DEFAULT_MESSAGE_LIMIT, MAX_MESSAGE_LIMIT);
        Pageable pageable = PageRequest.of(0, cappedLimit);
        List<ChatMessage> messages;

        if (after != null) {
            messages = messageRepository.findByConversationIdAndIdGreaterThanOrderByIdAsc(
                    conversationId, after, pageable);
        } else if (before != null) {
            messages = messageRepository.findByConversationIdAndIdLessThanOrderByIdDesc(
                    conversationId, before, pageable);
            Collections.reverse(messages);
        } else {
            messages = messageRepository.findByConversationIdOrderByIdDesc(conversationId, pageable);
            Collections.reverse(messages);
        }

        return new ChatMessageListResponse(conversationId, chatMapper.toMessages(messages));
    }

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(UUID conversationId, ChatMessageRequest request) {
        UUID userId = SecurityUtils.requireAuthenticatedUserId();
        if (request == null) {
            throw new BadRequestException("Message content cannot be blank");
        }
        validateContent(request.getContent());

        ChatMessage message = persistParticipantMessage(conversationId, userId, request.getContent());
        return chatMapper.toMessage(message);
    }

    @Override
    @Transactional
    public ChatReadResponse markAsRead(UUID conversationId, UUID upToMessageId) {
        UUID userId = SecurityUtils.requireAuthenticatedUserId();
        ChatConversation conversation = conversationRepository.findByIdAndParticipantForUpdate(conversationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with id: " + conversationId));

        UUID watermark;
        if (upToMessageId != null) {
            messageRepository.findByIdAndConversationId(upToMessageId, conversationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + upToMessageId));
            watermark = upToMessageId;
        } else {
            watermark = messageRepository.findMaxIdByConversationId(conversationId);
        }

        if (watermark != null) {
            messageRepository.markCounterpartMessagesReadUpTo(conversationId, userId, watermark);
        }

        int remaining = (int) messageRepository.countUnreadFromCounterpart(conversationId, userId);
        if (conversation.getBuyer().getId().equals(userId)) {
            conversation.setBuyerUnreadCount(remaining);
        } else {
            conversation.setSellerUnreadCount(remaining);
        }
        conversationRepository.saveAndFlush(conversation);
        return new ChatReadResponse(conversationId, remaining);
    }

    private ChatConversationDetailResponse toDetailForOffer(UUID offerId, UUID userId) {
        return conversationRepository.findByOfferIdAndBuyerId(offerId, userId)
                .map(conversation -> chatMapper.toDetail(conversation, userId))
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found with id: " + offerId));
    }

    private void appendInitialMessageIfPresent(UUID conversationId, UUID senderId, String initialMessage) {
        if (!StringUtils.hasText(initialMessage)) {
            return;
        }
        new TransactionTemplate(transactionManager).executeWithoutResult(status ->
                persistParticipantMessage(conversationId, senderId, initialMessage)
        );
    }

    private ChatMessage persistParticipantMessage(UUID conversationId, UUID senderId, String content) {
        ChatConversation conversation = conversationRepository.findByIdAndParticipantForUpdate(conversationId, senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with id: " + conversationId));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found User with uuid = " + senderId));
        ChatMessage message = persistMessage(conversation, sender, content);
        incrementCounterpartUnread(conversation, senderId);
        conversationRepository.save(conversation);
        return message;
    }

    private void persistNewConversation(Offer offer, User buyer, String initialMessage) {
        ChatConversation conversation = new ChatConversation();
        conversation.setOffer(offer);
        conversation.setSeller(offer.getUser());
        conversation.setBuyer(buyer);
        conversation = conversationRepository.saveAndFlush(conversation);

        if (StringUtils.hasText(initialMessage)) {
            persistMessage(conversation, buyer, initialMessage);
            incrementCounterpartUnread(conversation, buyer.getId());
            conversationRepository.saveAndFlush(conversation);
        }
    }

    private ChatMessage persistMessage(ChatConversation conversation, User sender, String content) {
        ChatMessage message = new ChatMessage();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(content);
        message = messageRepository.saveAndFlush(message);

        LocalDateTime sentAt = message.getCreatedDate() != null ? message.getCreatedDate() : LocalDateTime.now();
        conversation.setLastMessageAt(sentAt);
        conversation.setLastMessagePreview(truncatePreview(content));
        return message;
    }

    private void incrementCounterpartUnread(ChatConversation conversation, UUID senderId) {
        if (conversation.getBuyer().getId().equals(senderId)) {
            conversation.setSellerUnreadCount(conversation.getSellerUnreadCount() + 1);
        } else {
            conversation.setBuyerUnreadCount(conversation.getBuyerUnreadCount() + 1);
        }
    }

    private void validateContent(String content) {
        if (!StringUtils.hasText(content)) {
            throw new BadRequestException("Message content cannot be blank");
        }
        validateContentLength(content);
    }

    private void validateContentLength(String content) {
        if (content.length() > CONTENT_MAX_LENGTH) {
            throw new BadRequestException("Message content must be at most 2000 characters");
        }
    }

    private String truncatePreview(String content) {
        if (content.length() <= PREVIEW_MAX_LENGTH) {
            return content;
        }
        return content.substring(0, PREVIEW_MAX_LENGTH);
    }

    private boolean includeBuyer(ChatParticipantRole role) {
        return role == null || role == ChatParticipantRole.BUYER;
    }

    private boolean includeSeller(ChatParticipantRole role) {
        return role == null || role == ChatParticipantRole.SELLER;
    }

    private int normalizePage(Integer page) {
        if (page == null || page < 0) {
            return 0;
        }
        return page;
    }

    private int capLimit(Integer value, int defaultValue, int maxValue) {
        if (value == null || value < 1) {
            return defaultValue;
        }
        return Math.min(value, maxValue);
    }
}
