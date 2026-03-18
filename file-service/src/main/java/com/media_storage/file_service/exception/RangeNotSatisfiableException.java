package com.media_storage.file_service.exception;

public class RangeNotSatisfiableException extends RuntimeException {
    public RangeNotSatisfiableException(String message) {
        super(message);
    }
}
