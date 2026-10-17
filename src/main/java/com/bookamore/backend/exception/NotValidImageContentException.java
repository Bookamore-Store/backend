package com.bookamore.backend.exception;

public class NotValidImageContentException extends RuntimeException {
    public NotValidImageContentException(String message) {
        super(message);
    }

    public NotValidImageContentException(String message, Throwable cause) {
        super(message, cause);
    }
}
