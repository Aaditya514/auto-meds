package com.automeds.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: CheckoutRequest
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public class CheckoutRequest {

    @NotBlank(message = "Delivery address is required")
    private String deliveryAddress;

    private String paymentMethod; // "CASH_ON_DELIVERY", "MOCK_ONLINE"

    public CheckoutRequest() {
        this.paymentMethod = "CASH_ON_DELIVERY";
    }

    public CheckoutRequest(String deliveryAddress, String paymentMethod) {
        this.deliveryAddress = deliveryAddress;
        this.paymentMethod = paymentMethod != null ? paymentMethod : "CASH_ON_DELIVERY";
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    @Override
    public String toString() {
        return "CheckoutRequest{" +
                "deliveryAddress='" + deliveryAddress + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                '}';
    }
}
