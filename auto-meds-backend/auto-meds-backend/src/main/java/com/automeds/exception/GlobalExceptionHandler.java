package com.automeds.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_STATUS = "status";
    private static final String KEY_ERROR = "error";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_DETAIL = "detail";
    private static final String KEY_TITLE = "title";
    private static final String KEY_TYPE = "type";

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put(KEY_TYPE, "https://automeds.com/errors/not-found");
        body.put(KEY_TITLE, "Resource Not Found");
        body.put(KEY_TIMESTAMP, LocalDateTime.now());
        body.put(KEY_STATUS, HttpStatus.NOT_FOUND.value());
        body.put(KEY_ERROR, "Not Found");
        body.put(KEY_MESSAGE, ex.getMessage());
        body.put(KEY_DETAIL, ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({BadRequestException.class, InsufficientStockException.class})
    public ResponseEntity<Map<String, Object>> handleBadRequest(RuntimeException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put(KEY_TYPE, "https://automeds.com/errors/bad-request");
        body.put(KEY_TITLE, "Bad Request");
        body.put(KEY_TIMESTAMP, LocalDateTime.now());
        body.put(KEY_STATUS, HttpStatus.BAD_REQUEST.value());
        body.put(KEY_ERROR, "Bad Request");
        body.put(KEY_MESSAGE, ex.getMessage());
        body.put(KEY_DETAIL, ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put(KEY_TYPE, "https://automeds.com/errors/forbidden");
        body.put(KEY_TITLE, "Access Denied");
        body.put(KEY_TIMESTAMP, LocalDateTime.now());
        body.put(KEY_STATUS, HttpStatus.FORBIDDEN.value());
        body.put(KEY_ERROR, "Forbidden");
        body.put(KEY_MESSAGE, "You do not have permission to access this resource.");
        body.put(KEY_DETAIL, ex.getMessage() != null ? ex.getMessage() : "Access is denied.");
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(AuthenticationException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put(KEY_TYPE, "https://automeds.com/errors/unauthorized");
        body.put(KEY_TITLE, "Unauthorized");
        body.put(KEY_TIMESTAMP, LocalDateTime.now());
        body.put(KEY_STATUS, HttpStatus.UNAUTHORIZED.value());
        body.put(KEY_ERROR, "Unauthorized");
        body.put(KEY_MESSAGE, "Authentication failed. Please log in with valid credentials.");
        body.put(KEY_DETAIL, ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        Map<String, Object> body = new HashMap<>();
        body.put(KEY_TYPE, "https://automeds.com/errors/validation-failed");
        body.put(KEY_TITLE, "Validation Failed");
        body.put(KEY_TIMESTAMP, LocalDateTime.now());
        body.put(KEY_STATUS, HttpStatus.BAD_REQUEST.value());
        body.put(KEY_ERROR, "Validation Failed");
        body.put(KEY_MESSAGE, "Input validation failed. Please review the provided fields.");
        body.put(KEY_DETAIL, "One or more fields failed validation.");
        body.put("validationErrors", errors);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(Exception ex) {
        logger.error("Unhandled internal server error occurred", ex);

        Map<String, Object> body = new HashMap<>();
        body.put(KEY_TYPE, "https://automeds.com/errors/internal-error");
        body.put(KEY_TITLE, "Internal Server Error");
        body.put(KEY_TIMESTAMP, LocalDateTime.now());
        body.put(KEY_STATUS, HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put(KEY_ERROR, "Internal Server Error");
        body.put(KEY_MESSAGE, ex.getMessage() != null ? ex.getMessage() : "An unexpected server error occurred.");
        body.put(KEY_DETAIL, "An unexpected server error occurred. Please contact support if the issue persists.");
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
