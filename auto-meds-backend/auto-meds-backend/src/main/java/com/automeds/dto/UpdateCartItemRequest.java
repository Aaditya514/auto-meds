package com.automeds.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: UpdateCartItemRequest
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public class UpdateCartItemRequest {

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    public UpdateCartItemRequest() {
    }

    public UpdateCartItemRequest(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "UpdateCartItemRequest{" +
                "quantity=" + quantity +
                '}';
    }
}
