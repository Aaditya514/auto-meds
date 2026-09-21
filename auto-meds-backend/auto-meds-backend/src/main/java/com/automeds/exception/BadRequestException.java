package com.automeds.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: BadRequestException
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
