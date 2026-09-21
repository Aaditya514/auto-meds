package com.automeds.service;

import com.automeds.dto.AdminDashboardDTO;
import com.automeds.dto.MedicineDTO;
import com.automeds.dto.OrderDTO;
import com.automeds.dto.SubscriptionResponseDTO;
import com.automeds.entity.Medicine;
import com.automeds.entity.Order;
import com.automeds.entity.Subscription;
import com.automeds.entity.User;
import com.automeds.exception.BadRequestException;
import com.automeds.exception.ResourceNotFoundException;
import com.automeds.repository.MedicineRepository;
import com.automeds.repository.OrderRepository;
import com.automeds.repository.SubscriptionRepository;
import com.automeds.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: AdminService
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public class AdminService {

    private final UserRepository userRepository;
    private final MedicineRepository medicineRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final OrderRepository orderRepository;
    private final MedicineService medicineService;
    private final SubscriptionService subscriptionService;
    private final OrderService orderService;
    private final NotificationService notificationService;
    private final EmailService emailService;

    public AdminService(UserRepository userRepository, MedicineRepository medicineRepository, SubscriptionRepository subscriptionRepository, OrderRepository orderRepository, MedicineService medicineService, SubscriptionService subscriptionService, OrderService orderService, NotificationService notificationService, EmailService emailService) {
        this.userRepository = userRepository;
        this.medicineRepository = medicineRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.orderRepository = orderRepository;
        this.medicineService = medicineService;
        this.subscriptionService = subscriptionService;
        this.orderService = orderService;
        this.notificationService = notificationService;
        this.emailService = emailService;
    }

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_PENDING = "PENDING";
    private static final String ENTITY_SUBSCRIPTION = "Subscription";
    private static final String ENTITY_MEDICINE = "Medicine";

    // Wraps execution inside a database transaction
    @Transactional(readOnly = true)
    public AdminDashboardDTO getDashboardMetrics() {
        Long totalPatients = (long) userRepository.findByRole("PATIENT").size();
        Long totalMedicines = medicineRepository.count();
        Long activeSubscriptions = subscriptionRepository.countByStatus(STATUS_ACTIVE);
        Long pendingRequests = subscriptionRepository.countByStatus(STATUS_PENDING);
        Long upcomingRefills = (long) subscriptionRepository.findByStatusAndNextRefillDateLessThanEqual(STATUS_ACTIVE, LocalDateTime.now().plusDays(7)).size();
        Long pendingOrders = orderRepository.countByOrderStatus(STATUS_PENDING);
        Long lowStockMedicines = (long) medicineRepository.findByActiveAndStockQuantityLessThanEqual(1, 10).size();
        Long outOfStockMedicines = (long) medicineRepository.findByActiveAndStockQuantityEquals(1, 0).size();

        return new AdminDashboardDTO(
                totalPatients, totalMedicines, activeSubscriptions, pendingRequests,
                upcomingRefills, pendingOrders, lowStockMedicines, outOfStockMedicines
        );
    }

    // Wraps execution inside a database transaction
    @Transactional(readOnly = true)
    public List<SubscriptionResponseDTO> getPendingSubscriptionRequests() {
        return subscriptionRepository.findByStatus(STATUS_PENDING).stream()
                .map(subscriptionService::convertToDTO)
                .toList();
    }

    // Wraps execution inside a database transaction
    @Transactional
    public List<SubscriptionResponseDTO> approveSubscription(Long subscriptionId, List<com.automeds.dto.MedicineAssignmentDTO> assignments) {
        Subscription primarySub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_SUBSCRIPTION, "id", subscriptionId));

        if (assignments == null || assignments.isEmpty()) {
            throw new BadRequestException("Please select at least one medicine from the catalog to assign to this subscription.");
        }

        List<SubscriptionResponseDTO> approvedSubs = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < assignments.size(); i++) {
            com.automeds.dto.MedicineAssignmentDTO assignment = assignments.get(i);
            Long medId = assignment.getMedicineId();
            Medicine medicine = medicineRepository.findById(medId)
                    .orElseThrow(() -> new ResourceNotFoundException(ENTITY_MEDICINE, "id", medId));

            Subscription targetSub;
            if (i == 0) {
                targetSub = primarySub;
            } else {
                targetSub = new Subscription();
                targetSub.setPatient(primarySub.getPatient());
                targetSub.setPrescription(primarySub.getPrescription());
            }

            targetSub.setDosage(assignment.getDosage());
            targetSub.setFrequency(assignment.getFrequency());
            targetSub.setQuantity(assignment.getQuantity());

            targetSub.setMedicine(medicine);
            targetSub.setStatus(STATUS_ACTIVE);
            targetSub.setStartDate(now);

            int durationDays = subscriptionService.calculateDurationDays(targetSub.getQuantity(), targetSub.getDosage());
            LocalDateTime nextRefill = now.plusDays(durationDays);
            targetSub.setNextRefillDate(nextRefill);
            targetSub.setNextDispatchDate(nextRefill.minusDays(5));

            Subscription saved = subscriptionRepository.save(targetSub);
            approvedSubs.add(subscriptionService.convertToDTO(saved));
        }

        String medNames = approvedSubs.stream().map(SubscriptionResponseDTO::getMedicineName).collect(Collectors.joining(", "));
        notificationService.createNotification(primarySub.getPatient().getId(), "Subscriptions Approved!", 
                "Your prescription subscription for (" + medNames + ") has been APPROVED. Next refill date: " + primarySub.getNextRefillDate().toLocalDate(), "SUCCESS");

        return approvedSubs;
    }

    // Wraps execution inside a database transaction
    @Transactional
    public SubscriptionResponseDTO rejectSubscription(Long subscriptionId, String reason) {
        Subscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_SUBSCRIPTION, "id", subscriptionId));

        sub.setStatus("REJECTED");
        Subscription saved = subscriptionRepository.save(sub);

        String medName = sub.getMedicine() != null ? sub.getMedicine().getMedicineName() : "Prescription Subscription";
        String message = "Your subscription request for " + medName + " was rejected.";
        if (reason != null && !reason.trim().isEmpty()) {
            message += " Reason: " + reason;
        }

        notificationService.createNotification(sub.getPatient().getId(), "Subscription Rejected", message, "DANGER");
        emailService.sendSubscriptionNotificationEmail(sub.getPatient(), "Subscription Request Rejected", message);
        return subscriptionService.convertToDTO(saved);
    }

    // Wraps execution inside a database transaction
    @Transactional
    public SubscriptionResponseDTO requestClarification(Long subscriptionId, String message) {
        Subscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_SUBSCRIPTION, "id", subscriptionId));

        sub.setStatus("CLARIFICATION_REQUIRED");
        Subscription saved = subscriptionRepository.save(sub);

        String medName = sub.getMedicine() != null ? sub.getMedicine().getMedicineName() : "Prescription Subscription";
        String notifMsg = "Clarification required for your subscription (" + medName + "): " + (message != null ? message : "Please re-upload a valid prescription.");
        notificationService.createNotification(sub.getPatient().getId(), "Subscription Clarification Required", notifMsg, "WARNING");
        emailService.sendSubscriptionNotificationEmail(sub.getPatient(), "Subscription Clarification Required", notifMsg);

        return subscriptionService.convertToDTO(saved);
    }

    // Wraps execution inside a database transaction
    @Transactional
    public MedicineDTO createMedicine(MedicineDTO dto) {
        Medicine medicine = new Medicine();
        medicine.setMedicineName(dto.getMedicineName());
        medicine.setBrandName(dto.getBrandName());
        medicine.setComposition(dto.getComposition());
        medicine.setStrength(dto.getStrength());
        medicine.setCategory(dto.getCategory());
        medicine.setPrice(dto.getPrice());
        medicine.setStockQuantity(dto.getStockQuantity());
        medicine.setRequiresPrescription(dto.getRequiresPrescription() != null && dto.getRequiresPrescription() ? 1 : 0);
        medicine.setDescription(dto.getDescription());
        medicine.setManufacturer(dto.getManufacturer());
        medicine.setExpiryDate(dto.getExpiryDate());
        medicine.setActive(1);

        return medicineService.convertToDTO(medicineRepository.save(medicine));
    }

    // Wraps execution inside a database transaction
    @Transactional
    public MedicineDTO updateMedicine(Long id, MedicineDTO dto) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_MEDICINE, "id", id));

        medicine.setMedicineName(dto.getMedicineName());
        medicine.setBrandName(dto.getBrandName());
        medicine.setComposition(dto.getComposition());
        medicine.setStrength(dto.getStrength());
        medicine.setCategory(dto.getCategory());
        medicine.setPrice(dto.getPrice());
        medicine.setStockQuantity(dto.getStockQuantity());
        medicine.setRequiresPrescription(dto.getRequiresPrescription() != null && dto.getRequiresPrescription() ? 1 : 0);
        medicine.setDescription(dto.getDescription());
        medicine.setManufacturer(dto.getManufacturer());
        if (dto.getExpiryDate() != null) {
            medicine.setExpiryDate(dto.getExpiryDate());
        }

        return medicineService.convertToDTO(medicineRepository.save(medicine));
    }

    // Wraps execution inside a database transaction
    @Transactional
    public MedicineDTO deactivateMedicine(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_MEDICINE, "id", id));

        medicine.setActive(0);
        return medicineService.convertToDTO(medicineRepository.save(medicine));
    }

    // Wraps execution inside a database transaction
    @Transactional
    public MedicineDTO updateStock(Long medicineId, Integer stockQuantity) {
        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_MEDICINE, "id", medicineId));

        if (stockQuantity < 0) {
            throw new BadRequestException("Stock quantity cannot be negative.");
        }

        medicine.setStockQuantity(stockQuantity);
        return medicineService.convertToDTO(medicineRepository.save(medicine));
    }

    // Wraps execution inside a database transaction
    @Transactional(readOnly = true)
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc().stream()
                .map(orderService::convertToDTO)
                .toList();
    }

    // Wraps execution inside a database transaction
    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        String oldStatus = order.getOrderStatus();
        order.setOrderStatus(newStatus);
        if ("DELIVERED".equals(newStatus)) {
            order.setPaymentStatus("PAID");
        }

        Order saved = orderRepository.save(order);

        notificationService.createNotification(order.getPatient().getId(), "Order Status Updated", 
                "Your Order #" + order.getId() + " status is now " + newStatus, "INFO");
        emailService.sendOrderStatusUpdateEmail(order.getPatient(), saved, oldStatus, newStatus);

        return orderService.convertToDTO(saved);
    }

    // Wraps execution inside a database transaction
    @Transactional(readOnly = true)
    public List<User> getAllPatients() {
        return userRepository.findByRole("PATIENT");
    }
}
