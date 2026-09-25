package com.bookamore.backend.exception;

public class UnprocessableRequestException extends RuntimeException {
    public UnprocessableRequestException(String message) {
        super(message);
    }
}
