package com.automeds.controller;

import com.automeds.dto.DispensingSlipDTO;
import com.automeds.dto.PaymentRequestDTO;
import com.automeds.dto.PaymentResponseDTO;
import com.automeds.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/orders", "/api/payments"})
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Submit payment for an order by order ID in URL.
     */
    @PostMapping("/{id}/pay")
    public ResponseEntity<PaymentResponseDTO> payOrder(
            @PathVariable Long id,
            @Valid @RequestBody PaymentRequestDTO request) {
        return ResponseEntity.ok(paymentService.processPayment(id, request));
    }

    /**
     * Submit payment for an order with orderId inside request body (supports /api/payments/process).
     */
    @PostMapping("/process")
    public ResponseEntity<PaymentResponseDTO> processPayment(
            @Valid @RequestBody PaymentRequestDTO request) {
        Long orderId = request.getOrderId();
        return ResponseEntity.ok(paymentService.processPayment(orderId, request));
    }

    /**
     * Retrieve official Four-Eyes verified clinical dispensing and packing slip.
     */
    @GetMapping({"/{id}/dispensing-slip", "/dispensing-slip/{id}"})
    public ResponseEntity<DispensingSlipDTO> getDispensingSlip(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getDispensingSlip(id));
    }
}
