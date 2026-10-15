package com.bookamore.backend.exception;

public class UnsupportedSortFieldException extends RuntimeException {
    public UnsupportedSortFieldException(String field) {
        super("Unsupported sort field: " + field);
    }
}
