-- liquibase formatted sql

-- changeset Professional:009-add-image-order-index
ALTER TABLE "images" ADD COLUMN "order_index" INTEGER NOT NULL DEFAULT 0;
