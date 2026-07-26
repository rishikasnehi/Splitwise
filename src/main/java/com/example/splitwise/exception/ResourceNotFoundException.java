package com.example.splitwise.exception;

/**
 * Thrown when a requested Group (or other entity) doesn't exist.
 * Mapped to HTTP 404 by GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
