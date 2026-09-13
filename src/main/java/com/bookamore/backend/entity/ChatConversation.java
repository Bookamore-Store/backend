package com.bookamore.backend.entity;

import com.bookamore.backend.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "chat_conversations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"offer_id", "buyer_id"})
})
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class ChatConversation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offer_id", nullable = false)
    @ToString.Exclude
    private Offer offer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    @ToString.Exclude
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_id", nullable = false)
    @ToString.Exclude
    private User buyer;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "last_message_preview")
    private String lastMessagePreview;

    @Column(name = "seller_unread_count", nullable = false)
    private int sellerUnreadCount = 0;

    @Column(name = "buyer_unread_count", nullable = false)
    private int buyerUnreadCount = 0;
}
