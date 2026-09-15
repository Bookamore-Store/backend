-- liquibase formatted sql

-- changeset Professional:004-create-chat-conversations
CREATE TABLE "chat_conversations" (
    "id" UUID NOT NULL,
    "offer_id" UUID NOT NULL,
    "seller_id" UUID NOT NULL,
    "buyer_id" UUID NOT NULL,
    "last_message_at" TIMESTAMP WITHOUT TIME ZONE,
    "last_message_preview" VARCHAR(255),
    "seller_unread_count" INTEGER NOT NULL DEFAULT 0,
    "buyer_unread_count" INTEGER NOT NULL DEFAULT 0,
    "created_date" TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    "last_modified_date" TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT "chat_conversations_pkey" PRIMARY KEY ("id"),
    CONSTRAINT "uk_chat_conversations_offer_buyer" UNIQUE ("offer_id", "buyer_id")
);

-- changeset Professional:004-create-chat-messages
CREATE TABLE "chat_messages" (
    "id" UUID NOT NULL,
    "conversation_id" UUID NOT NULL,
    "sender_id" UUID NOT NULL,
    "content" VARCHAR(2000) NOT NULL,
    "is_read" BOOLEAN NOT NULL DEFAULT FALSE,
    "created_date" TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    "last_modified_date" TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT "chat_messages_pkey" PRIMARY KEY ("id")
);

-- changeset Professional:004-chat-foreign-keys
ALTER TABLE "chat_conversations" ADD CONSTRAINT "fk_chat_conversations_offer"
    FOREIGN KEY ("offer_id") REFERENCES "offers" ("id") ON UPDATE NO ACTION ON DELETE CASCADE;

ALTER TABLE "chat_conversations" ADD CONSTRAINT "fk_chat_conversations_seller"
    FOREIGN KEY ("seller_id") REFERENCES "users" ("id") ON UPDATE NO ACTION ON DELETE CASCADE;

ALTER TABLE "chat_conversations" ADD CONSTRAINT "fk_chat_conversations_buyer"
    FOREIGN KEY ("buyer_id") REFERENCES "users" ("id") ON UPDATE NO ACTION ON DELETE CASCADE;

ALTER TABLE "chat_messages" ADD CONSTRAINT "fk_chat_messages_conversation"
    FOREIGN KEY ("conversation_id") REFERENCES "chat_conversations" ("id") ON UPDATE NO ACTION ON DELETE CASCADE;

ALTER TABLE "chat_messages" ADD CONSTRAINT "fk_chat_messages_sender"
    FOREIGN KEY ("sender_id") REFERENCES "users" ("id") ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset Professional:004-chat-indexes
-- Inbox DESC: PostgreSQL defaults to NULLS FIRST; NULLS LAST keeps empty threads (NULL last_message_at) at the end.
CREATE INDEX "idx_chat_messages_conversation_id" ON "chat_messages" ("conversation_id", "id" ASC);
CREATE INDEX "idx_chat_conversations_seller" ON "chat_conversations" ("seller_id", "last_message_at" DESC NULLS LAST);
CREATE INDEX "idx_chat_conversations_buyer" ON "chat_conversations" ("buyer_id", "last_message_at" DESC NULLS LAST);
