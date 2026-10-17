package com.bookamore.backend.exception;

public class ImageTooLargeException extends RuntimeException {
    public ImageTooLargeException(String message) {
        super(message);
    }
}
