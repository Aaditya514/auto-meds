package com.automeds.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: InsufficientStockException
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String message) {
        super(message);
    }
}
