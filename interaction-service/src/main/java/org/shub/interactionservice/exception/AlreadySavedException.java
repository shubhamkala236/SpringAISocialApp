package org.shub.interactionservice.exception;

public class AlreadySavedException extends RuntimeException {
    public AlreadySavedException(String message) {
        super(message);
    }
}