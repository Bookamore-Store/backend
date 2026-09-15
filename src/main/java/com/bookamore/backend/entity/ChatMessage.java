package com.bookamore.backend.entity;

import com.bookamore.backend.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "chat_messages")
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class ChatMessage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    @ToString.Exclude
    private ChatConversation conversation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    @ToString.Exclude
    private User sender;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;
}
