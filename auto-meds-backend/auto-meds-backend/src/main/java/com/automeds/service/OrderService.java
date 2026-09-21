package com.automeds.service;

import com.automeds.dto.CheckoutRequest;
import com.automeds.dto.OrderDTO;
import com.automeds.dto.OrderItemDTO;
import com.automeds.entity.*;
import com.automeds.exception.BadRequestException;
import com.automeds.exception.InsufficientStockException;
import com.automeds.exception.ResourceNotFoundException;
import com.automeds.repository.MedicineRepository;
import com.automeds.repository.OrderRepository;
import com.automeds.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: OrderService
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final UserRepository userRepository;
    private final MedicineRepository medicineRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    public OrderService(OrderRepository orderRepository, CartService cartService, UserRepository userRepository, MedicineRepository medicineRepository, NotificationService notificationService, EmailService emailService) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.userRepository = userRepository;
        this.medicineRepository = medicineRepository;
        this.notificationService = notificationService;
        this.emailService = emailService;
    }
    private static final String STATUS_PENDING = "PENDING";

    // Wraps execution inside a database transaction
    @Transactional
    public OrderDTO checkoutCart(Long patientId, CheckoutRequest request) {
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", patientId));

        Cart cart = cartService.getOrCreateCartForPatient(patientId);
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BadRequestException("Your cart is empty. Cannot proceed to checkout.");
        }

        // Re-validate stock for every item at checkout
        BigDecimal subtotalSum = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        Order order = new Order();
        order.setPatient(patient);
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "CASH_ON_DELIVERY");
        order.setPaymentStatus(STATUS_PENDING);
        order.setOrderStatus(STATUS_PENDING);
        order.setOrderType("ONE_TIME");
        order.setOrderDate(LocalDateTime.now());
        order.setExpectedDeliveryDate(LocalDateTime.now().plusDays(3));

        for (CartItem cartItem : cart.getItems()) {
            Medicine medicine = cartItem.getMedicine();

            if (medicine.getActive() == 0) {
                throw new BadRequestException("Medicine '" + medicine.getMedicineName() + "' is no longer active.");
            }

            if (medicine.getStockQuantity() < cartItem.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for '" + medicine.getMedicineName() + "'. Available: " + medicine.getStockQuantity() + ", Requested: " + cartItem.getQuantity());
            }

            BigDecimal lineSubtotal = medicine.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            subtotalSum = subtotalSum.add(lineSubtotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setMedicine(medicine);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(medicine.getPrice());
            orderItem.setSubtotal(lineSubtotal);
            orderItems.add(orderItem);

            // Deduct stock quantity
            medicine.setStockQuantity(medicine.getStockQuantity() - cartItem.getQuantity());
            medicineRepository.save(medicine);
        }

        BigDecimal deliveryFee = new BigDecimal("40.00");
        BigDecimal totalAmount = subtotalSum.add(deliveryFee);

        order.setTotalAmount(totalAmount);
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        // Clear cart
        cartService.clearCart(patientId);

        // Notifications & Email Dispatch
        notificationService.createNotification(patientId, "Order Placed Successfully", 
                "Order #" + savedOrder.getId() + " placed for total ₹" + totalAmount + ". Delivery expected by " + savedOrder.getExpectedDeliveryDate().toLocalDate(), "SUCCESS");
        emailService.sendOrderConfirmationEmail(patient, savedOrder);

        List<User> admins = userRepository.findByRole("ADMIN");
        for (User admin : admins) {
            notificationService.createNotification(admin.getId(), "New One-Time Order", 
                    "New Order #" + savedOrder.getId() + " received from " + patient.getName(), "INFO");
        }

        return convertToDTO(savedOrder);
    }

    // Wraps execution inside a database transaction
    @Transactional
    public Order createSubscriptionRefillOrder(Subscription sub, Medicine med) {
        User patient = sub.getPatient();
        BigDecimal lineSubtotal = med.getPrice().multiply(BigDecimal.valueOf(sub.getQuantity()));
        BigDecimal deliveryFee = new BigDecimal("40.00");
        BigDecimal totalAmount = lineSubtotal.add(deliveryFee);

        Order order = new Order();
        order.setPatient(patient);
        order.setSubscription(sub);
        order.setDeliveryAddress(patient.getAddress() != null ? patient.getAddress() + ", " + patient.getCity() : "Default Address");
        order.setPaymentMethod("AUTO_REFILL_COD");
        order.setPaymentStatus(STATUS_PENDING);
        order.setOrderStatus("APPROVED"); // Auto refill orders pre-approved
        order.setOrderType("SUBSCRIPTION_REFILL");
        order.setOrderDate(LocalDateTime.now());
        order.setExpectedDeliveryDate(LocalDateTime.now().plusDays(3));
        order.setTotalAmount(totalAmount);

        List<OrderItem> items = new ArrayList<>();
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setMedicine(med);
        item.setQuantity(sub.getQuantity());
        item.setPrice(med.getPrice());
        item.setSubtotal(lineSubtotal);
        items.add(item);

        order.setItems(items);

        // Deduct stock
        med.setStockQuantity(med.getStockQuantity() - sub.getQuantity());
        medicineRepository.save(med);

        Order savedOrder = orderRepository.save(order);

        notificationService.createNotification(patient.getId(), "Auto-Refill Order Generated", 
                "Automatic refill Order #" + savedOrder.getId() + " generated for " + med.getMedicineName() + ". Total: ₹" + totalAmount, "SUCCESS");
        emailService.sendSubscriptionRefillEmail(patient, savedOrder);

        return savedOrder;
    }

    // Wraps execution inside a database transaction
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersForPatient(Long patientId) {
        return orderRepository.findByPatientIdOrderByOrderDateDesc(patientId).stream()
                .map(this::convertToDTO)
                .toList();
    }

    // Wraps execution inside a database transaction
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return convertToDTO(order);
    }

    public OrderDTO convertToDTO(Order order) {
        List<OrderItemDTO> itemDTOs = order.getItems().stream()
                .map(item -> new OrderItemDTO(
                        item.getId(),
                        item.getMedicine().getId(),
                        item.getMedicine().getMedicineName(),
                        item.getMedicine().getBrandName(),
                        item.getMedicine().getComposition(),
                        item.getMedicine().getStrength(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getSubtotal()
                ))
                .toList();

        return new OrderDTO(
                order.getId(),
                order.getPatient().getId(),
                order.getPatient().getName(),
                order.getPatient().getEmail(),
                order.getSubscription() != null ? order.getSubscription().getId() : null,
                order.getOrderDate(),
                order.getTotalAmount(),
                order.getDeliveryAddress(),
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                order.getOrderStatus(),
                order.getOrderType(),
                order.getExpectedDeliveryDate(),
                itemDTOs
        );
    }
}
