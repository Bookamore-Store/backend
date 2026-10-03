-- liquibase formatted sql

-- changeset Professional:005-add-user-password-reset-columns
ALTER TABLE "users" ADD COLUMN "password_reset_code" VARCHAR(255);
ALTER TABLE "users" ADD COLUMN "password_reset_code_expiration_time" TIMESTAMP WITHOUT TIME ZONE;
