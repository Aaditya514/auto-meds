package com.automeds.service;

import com.automeds.dto.AdminDashboardDTO;
import com.automeds.dto.DeficitSubscriptionDTO;
import com.automeds.dto.MedicineDTO;
import com.automeds.dto.OrderDTO;
import com.automeds.dto.ProcurementAlertDTO;
import com.automeds.dto.RestockRequestDTO;
import com.automeds.dto.RestockResponseDTO;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
    private final SseNotificationService sseNotificationService;

    @org.springframework.beans.factory.annotation.Autowired
    public AdminService(UserRepository userRepository, MedicineRepository medicineRepository, SubscriptionRepository subscriptionRepository, OrderRepository orderRepository, MedicineService medicineService, SubscriptionService subscriptionService, OrderService orderService, NotificationService notificationService, EmailService emailService, SseNotificationService sseNotificationService) {
        this.userRepository = userRepository;
        this.medicineRepository = medicineRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.orderRepository = orderRepository;
        this.medicineService = medicineService;
        this.subscriptionService = subscriptionService;
        this.orderService = orderService;
        this.notificationService = notificationService;
        this.emailService = emailService;
        this.sseNotificationService = sseNotificationService;
    }

    public AdminService(UserRepository userRepository, MedicineRepository medicineRepository, SubscriptionRepository subscriptionRepository, OrderRepository orderRepository, MedicineService medicineService, SubscriptionService subscriptionService, OrderService orderService, NotificationService notificationService, EmailService emailService) {
        this(userRepository, medicineRepository, subscriptionRepository, orderRepository, medicineService, subscriptionService, orderService, notificationService, emailService, null);
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
        Long deficitSubscriptions = (long) subscriptionRepository.findByStatusAndReservationStatus(STATUS_ACTIVE, "OUT_OF_STOCK_DEFICIT").size();
        Long procurementAlerts = (long) medicineRepository.findMedicinesNeedingReorder().size();

        return new AdminDashboardDTO(
                totalPatients, totalMedicines, activeSubscriptions, pendingRequests,
                upcomingRefills, pendingOrders, lowStockMedicines, outOfStockMedicines,
                deficitSubscriptions, procurementAlerts
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

        // P4: Live SSE push to patient
        if (sseNotificationService != null) {
            sseNotificationService.sendToPatient(primarySub.getPatient().getId(), "SUBSCRIPTION_APPROVED", java.util.Map.of(
                    "subscriptionId", subscriptionId,
                    "medicines", medNames,
                    "message", "Your medication prescription has been approved by the pharmacist!"
            ));
        }

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

        // P4: Live SSE push to patient
        if (sseNotificationService != null) {
            sseNotificationService.sendToPatient(sub.getPatient().getId(), "SUBSCRIPTION_REJECTED", java.util.Map.of(
                    "subscriptionId", subscriptionId,
                    "reason", reason != null ? reason : "Requires clinical review"
            ));
        }

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

        // P4: Live SSE push to patient
        if (sseNotificationService != null) {
            sseNotificationService.sendToPatient(sub.getPatient().getId(), "CLARIFICATION_REQUIRED", java.util.Map.of(
                    "subscriptionId", subscriptionId,
                    "message", message != null ? message : "Pharmacist requested clarification."
            ));
        }

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
        medicine.setSymptoms(dto.getSymptoms());
        medicine.setActive(1);

        medicineService.evictMedicineCaches();
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
        medicine.setSymptoms(dto.getSymptoms());
        if (dto.getExpiryDate() != null) {
            medicine.setExpiryDate(dto.getExpiryDate());
        }

        medicineService.evictMedicineCaches();
        return medicineService.convertToDTO(medicineRepository.save(medicine));
    }

    // Wraps execution inside a database transaction
    @Transactional
    public MedicineDTO deactivateMedicine(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_MEDICINE, "id", id));

        medicine.setActive(0);
        medicineService.evictMedicineCaches();
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
        medicineService.evictMedicineCaches();
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

        // P4: Live SSE push to patient on order status progression
        if (sseNotificationService != null) {
            sseNotificationService.sendToPatient(order.getPatient().getId(), "ORDER_STATUS_CHANGED", java.util.Map.of(
                    "orderId", order.getId(),
                    "oldStatus", oldStatus != null ? oldStatus : "",
                    "newStatus", newStatus,
                    "message", "Order #" + order.getId() + " status is now " + newStatus
            ));
        }

        return orderService.convertToDTO(saved);
    }

    // Wraps execution inside a database transaction
    @Transactional(readOnly = true)
    public List<User> getAllPatients() {
        return userRepository.findByRole("PATIENT");
    }

    /**
     * Retrieves inventory items reaching or below reorder threshold with active subscriber deficit counts.
     */
    @Transactional(readOnly = true)
    public List<ProcurementAlertDTO> getProcurementAlerts() {
        List<Medicine> needingReorder = medicineRepository.findMedicinesNeedingReorder();
        List<ProcurementAlertDTO> alerts = new ArrayList<>();

        for (Medicine m : needingReorder) {
            int available = m.getAvailableQuantity();
            Long deficitCount = subscriptionRepository.countByMedicineIdAndStatusAndReservationStatus(
                    m.getId(), STATUS_ACTIVE, "OUT_OF_STOCK_DEFICIT"
            );

            String urgency;
            if (m.getStockQuantity() == 0 || available == 0) {
                urgency = "CRITICAL_STOCKOUT";
            } else if (deficitCount > 0) {
                urgency = "DEFICIT_QUEUED";
            } else {
                urgency = "LOW_STOCK";
            }

            alerts.add(new ProcurementAlertDTO(
                    m.getId(),
                    m.getMedicineName(),
                    m.getBrandName(),
                    m.getComposition(),
                    m.getStrength(),
                    m.getPrice(),
                    m.getStockQuantity(),
                    m.getReservedQuantity(),
                    available,
                    m.getReorderThreshold(),
                    m.getSuggestedReorderPackSize(),
                    deficitCount,
                    urgency
            ));
        }

        alerts.sort((a, b) -> {
            int pA = "CRITICAL_STOCKOUT".equals(a.getUrgency()) ? 0 : ("DEFICIT_QUEUED".equals(a.getUrgency()) ? 1 : 2);
            int pB = "CRITICAL_STOCKOUT".equals(b.getUrgency()) ? 0 : ("DEFICIT_QUEUED".equals(b.getUrgency()) ? 1 : 2);
            return Integer.compare(pA, pB);
        });

        return alerts;
    }

    /**
     * Retrieves all active chronic subscriptions currently waiting for stock replenishment.
     */
    @Transactional(readOnly = true)
    public List<DeficitSubscriptionDTO> getDeficitSubscriptions() {
        List<Subscription> deficitSubs = subscriptionRepository.findByStatusAndReservationStatus(STATUS_ACTIVE, "OUT_OF_STOCK_DEFICIT");
        List<DeficitSubscriptionDTO> dtos = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Subscription sub : deficitSubs) {
            Medicine med = sub.getMedicine();
            long daysUntilRefill = sub.getNextRefillDate() != null
                    ? ChronoUnit.DAYS.between(today, sub.getNextRefillDate().toLocalDate())
                    : 0L;

            dtos.add(new DeficitSubscriptionDTO(
                    sub.getId(),
                    sub.getPatient().getId(),
                    sub.getPatient().getFullName(),
                    sub.getPatient().getEmail(),
                    med != null ? med.getId() : null,
                    med != null ? med.getMedicineName() : "Unknown Medicine",
                    sub.getQuantity(),
                    med != null ? med.getAvailableQuantity() : 0,
                    sub.getNextRefillDate(),
                    daysUntilRefill
            ));
        }

        dtos.sort((a, b) -> Long.compare(a.getDaysUntilRefill(), b.getDaysUntilRefill()));
        return dtos;
    }

    /**
     * Replenishes warehouse medicine inventory and executes closed-loop deficit fulfillment for waiting subscribers.
     */
    @Transactional
    public RestockResponseDTO restockMedicine(RestockRequestDTO request) {
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new BadRequestException("Restock quantity must be greater than zero.");
        }

        Medicine medicine = medicineRepository.findByIdForUpdate(request.getMedicineId())
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_MEDICINE, "id", request.getMedicineId()));

        int previousStock = medicine.getStockQuantity();
        int previousAvailable = medicine.getAvailableQuantity();

        medicine.setStockQuantity(previousStock + request.getQuantity());
        if (request.getExpiryDate() != null) {
            medicine.setExpiryDate(request.getExpiryDate().atStartOfDay());
        }
        medicineRepository.save(medicine);

        List<Subscription> deficitSubs = subscriptionRepository.findByMedicineIdAndStatusAndReservationStatusOrderByNextRefillDateAsc(
                medicine.getId(), STATUS_ACTIVE, "OUT_OF_STOCK_DEFICIT"
        );

        int deficitsResolved = 0;
        for (Subscription sub : deficitSubs) {
            if (medicine.getAvailableQuantity() >= sub.getQuantity()) {
                int rows = medicineRepository.reserveStock(medicine.getId(), sub.getQuantity());
                if (rows > 0) {
                    sub.setReservationStatus("RESERVED");
                    sub.setReservationDate(LocalDateTime.now());
                    subscriptionRepository.save(sub);
                    deficitsResolved++;

                    notificationService.createNotification(
                            sub.getPatient().getId(),
                            "🔒 Medication Restocked & Locked in Vault",
                            "Great news! Your chronic care medication " + medicine.getMedicineName() + " has been restocked, and " + sub.getQuantity() + " units have been reserved in our vault for your upcoming refill.",
                            "SUCCESS"
                    );

                    notificationService.notifyStaff(
                            "Deficit Fulfilled: " + medicine.getMedicineName(),
                            String.format("Subscription #%d for patient %s has been automatically allocated and soft-locked (%d units) following batch restock.",
                                    sub.getId(), sub.getPatient().getFullName(), sub.getQuantity()),
                            "INFO"
                    );

                    // P4: Live SSE push to patient on deficit fulfillment
                    if (sseNotificationService != null) {
                        sseNotificationService.sendToPatient(sub.getPatient().getId(), "DEFICIT_ALLOCATED", java.util.Map.of(
                                "subscriptionId", sub.getId(),
                                "medicineName", medicine.getMedicineName(),
                                "quantity", sub.getQuantity()
                        ));
                    }
                }
            }
        }

        // P3: Invalidate cached medicine catalog after restock
        medicineService.evictMedicineCaches();

        Medicine refreshed = medicineRepository.findById(medicine.getId()).orElse(medicine);

        // P4: Broadcast restock event to clinical staff command centers
        if (sseNotificationService != null) {
            sseNotificationService.sendToStaff("INVENTORY_RESTOCKED", java.util.Map.of(
                    "medicineId", medicine.getId(),
                    "medicineName", medicine.getMedicineName(),
                    "quantityRestocked", request.getQuantity(),
                    "availableQuantity", refreshed.getAvailableQuantity(),
                    "deficitsResolved", deficitsResolved
            ));
        }

        String msg = String.format("Successfully restocked %d units of %s. Current stock: %d (Available: %d, Reserved: %d). %d waiting subscriber deficits fulfilled.",
                request.getQuantity(), medicine.getMedicineName(), refreshed.getStockQuantity(), refreshed.getAvailableQuantity(), refreshed.getReservedQuantity(), deficitsResolved);

        return new RestockResponseDTO(
                medicine.getId(),
                medicine.getMedicineName(),
                previousStock,
                refreshed.getStockQuantity(),
                previousAvailable,
                refreshed.getAvailableQuantity(),
                deficitsResolved,
                msg
        );
    }
}
