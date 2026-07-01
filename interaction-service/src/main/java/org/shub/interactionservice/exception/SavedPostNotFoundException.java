package org.shub.interactionservice.exception;

public class SavedPostNotFoundException extends RuntimeException {
    public SavedPostNotFoundException(String message) {
        super(message);
    }
}