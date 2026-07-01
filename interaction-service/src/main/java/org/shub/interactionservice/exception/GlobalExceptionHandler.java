package org.shub.interactionservice.exception;

import org.shub.interactionservice.dto.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AlreadySavedException.class)
    public ResponseEntity<MessageResponse> handleAlreadySaved(AlreadySavedException ex) {
        return ResponseEntity.badRequest().body(new MessageResponse(ex.getMessage()));
    }

    @ExceptionHandler(SavedPostNotFoundException.class)
    public ResponseEntity<MessageResponse> handleSavedNotFound(SavedPostNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponse> handleGeneric(Exception ex) {
        return ResponseEntity.internalServerError().body(new MessageResponse(ex.getMessage()));
    }
}