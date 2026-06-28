package org.shub.authservice.exception;

import org.shub.authservice.dto.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Centralizes what each controller's try/catch did individually in the C#
 * version (AuthController wrapped every action in try { ... } catch (Exception
 * ex) { return BadRequest(...) / Unauthorized(...) }). Spring's
 * @RestControllerAdvice intercepts exceptions thrown anywhere in the request
 * pipeline, so the controller methods themselves stay free of boilerplate.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<MessageResponse> handleDuplicateEmail(DuplicateEmailException ex) {
        return ResponseEntity.badRequest().body(new MessageResponse(ex.getMessage()));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<MessageResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse(ex.getMessage()));
    }

    // Bean Validation failures (@NotBlank, @Email, @Size on request DTOs)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponse> handleGeneric(Exception ex) {
        ex.printStackTrace();  // TEMPORARY - prints full trace including all "Caused by" regardless of logging config
        return ResponseEntity.internalServerError().body(new MessageResponse(ex.getMessage()));
    }
}