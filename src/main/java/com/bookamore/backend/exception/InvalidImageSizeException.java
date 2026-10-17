package com.bookamore.backend.exception;

public class InvalidImageSizeException extends RuntimeException {
    public InvalidImageSizeException(String message) {
        super(message);
    }
}
