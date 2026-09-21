package com.automeds.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionsTest {

    @Test
    void testBadRequestException() {
        BadRequestException ex = new BadRequestException("Bad input");
        assertEquals("Bad input", ex.getMessage());
    }

    @Test
    void testResourceNotFoundException() {
        ResourceNotFoundException ex1 = new ResourceNotFoundException("Resource not found");
        assertEquals("Resource not found", ex1.getMessage());

        ResourceNotFoundException ex2 = new ResourceNotFoundException("User", "id", 123L);
        assertEquals("User not found with id : '123'", ex2.getMessage());
    }

    @Test
    void testInsufficientStockException() {
        InsufficientStockException ex = new InsufficientStockException("Insufficient stock for medicine");
        assertEquals("Insufficient stock for medicine", ex.getMessage());
    }
}


