package com.automeds.service;

import com.automeds.dto.SubscriptionRequestDTO;
import com.automeds.dto.SubscriptionResponseDTO;
import com.automeds.entity.*;
import com.automeds.exception.BadRequestException;
import com.automeds.exception.InsufficientStockException;
import com.automeds.exception.ResourceNotFoundException;
import com.automeds.repository.MedicineRepository;
import com.automeds.repository.OrderRepository;
import com.automeds.repository.SubscriptionRepository;
import com.automeds.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: SubscriptionService
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final MedicineRepository medicineRepository;
    private final UserRepository userRepository;
    private final PrescriptionService prescriptionService;
    private final NotificationService notificationService;
    private final OrderRepository orderRepository;
    private static final String ENTITY_SUBSCRIPTION = "Subscription";
    private static final String MSG_UNAUTHORIZED = "Unauthorized to modify this subscription.";
    private static final String MSG_SUB_FOR = "Your subscription for ";

    public SubscriptionService(SubscriptionRepository subscriptionRepository, 
                               MedicineRepository medicineRepository, 
                               UserRepository userRepository, 
                               PrescriptionService prescriptionService, 
                               NotificationService notificationService,
                               OrderRepository orderRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.medicineRepository = medicineRepository;
        this.userRepository = userRepository;
        this.prescriptionService = prescriptionService;
        this.notificationService = notificationService;
        this.orderRepository = orderRepository;
    }

    // Wraps execution inside a database transaction
    @Transactional
    public SubscriptionResponseDTO createSubscription(Long patientId, SubscriptionRequestDTO request, MultipartFile prescriptionFile) {
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", patientId));

        Medicine medicine = null;
        if (request.getMedicineId() != null) {
            medicine = medicineRepository.findById(request.getMedicineId()).orElse(null);
        }

        if (prescriptionFile == null || prescriptionFile.isEmpty()) {
            throw new BadRequestException("Prescription file is required for chronic medication subscription.");
        }

        Prescription prescription = prescriptionService.uploadPrescription(patientId, prescriptionFile, 6, request.getDoctorVisitDate());

        Subscription subscription = new Subscription();
        subscription.setPatient(patient);
        subscription.setMedicine(medicine);
        subscription.setPrescription(prescription);
        subscription.setDosage(request.getDosage());
        subscription.setFrequency(request.getFrequency());
        subscription.setQuantity(request.getQuantity());
        subscription.setStatus("PENDING");

        Subscription saved = subscriptionRepository.save(subscription);

        // Notify patient & admin
        notificationService.createNotification(patientId, "Subscription Request Submitted", 
                "Your subscription request with uploaded prescription has been submitted for Pharmacist review.", "INFO");

        List<User> admins = userRepository.findByRole("ADMIN");
        for (User admin : admins) {
            notificationService.createNotification(admin.getId(), "New Subscription Request", 
                    "New subscription request from " + patient.getName() + " with uploaded prescription.", "WARNING");
        }

        return convertToDTO(saved);
    }

    public List<SubscriptionResponseDTO> getSubscriptionsForPatient(Long patientId) {
        return subscriptionRepository.findByPatientIdOrderByCreatedAtDesc(patientId).stream()
                .map(this::convertToDTO)
                .toList();
    }

    public SubscriptionResponseDTO getSubscriptionById(Long id) {
        Subscription sub = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_SUBSCRIPTION, "id", id));
        return convertToDTO(sub);
    }

    public SubscriptionResponseDTO getSubscriptionById(Long id, com.automeds.security.UserPrincipal userPrincipal) {
        Subscription sub = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_SUBSCRIPTION, "id", id));
        if (userPrincipal != null) {
            boolean isStaff = userPrincipal.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_PHARMACIST"));
            if (!isStaff && !sub.getPatient().getId().equals(userPrincipal.getId())) {
                throw new BadRequestException(MSG_UNAUTHORIZED);
            }
        }
        return convertToDTO(sub);
    }

    // Wraps execution inside a database transaction
    @Transactional
    public SubscriptionResponseDTO cancelSubscription(Long patientId, Long subscriptionId) {
        Subscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_SUBSCRIPTION, "id", subscriptionId));

        if (!sub.getPatient().getId().equals(patientId)) {
            throw new BadRequestException(MSG_UNAUTHORIZED);
        }

        // If stock was soft-locked, release it back to general inventory
        if ("RESERVED".equals(sub.getReservationStatus()) && sub.getMedicine() != null && sub.getQuantity() != null) {
            medicineRepository.releaseReservedStock(sub.getMedicine().getId(), sub.getQuantity());
            sub.setReservationStatus("NONE");
        }

        sub.setStatus("CANCELLED");
        Subscription saved = subscriptionRepository.save(sub);

        notificationService.createNotification(patientId, "Subscription Cancelled", 
                MSG_SUB_FOR + (sub.getMedicine() != null ? sub.getMedicine().getMedicineName() : "medication") + " has been cancelled.", "INFO");

        return convertToDTO(saved);
    }

    // Wraps execution inside a database transaction
    @Transactional
    public SubscriptionResponseDTO pauseSubscription(Long patientId, Long subscriptionId) {
        Subscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_SUBSCRIPTION, "id", subscriptionId));

        if (!sub.getPatient().getId().equals(patientId)) {
            throw new BadRequestException(MSG_UNAUTHORIZED);
        }

        if (!"ACTIVE".equals(sub.getStatus())) {
            throw new BadRequestException("Only ACTIVE subscriptions can be paused.");
        }

        // Release soft-locked stock when paused
        if ("RESERVED".equals(sub.getReservationStatus()) && sub.getMedicine() != null && sub.getQuantity() != null) {
            medicineRepository.releaseReservedStock(sub.getMedicine().getId(), sub.getQuantity());
            sub.setReservationStatus("NONE");
        }

        sub.setStatus("PAUSED");
        Subscription saved = subscriptionRepository.save(sub);

        notificationService.createNotification(patientId, "Subscription Paused", 
                MSG_SUB_FOR + (sub.getMedicine() != null ? sub.getMedicine().getMedicineName() : "medication") + " has been paused.", "INFO");

        return convertToDTO(saved);
    }

    // Wraps execution inside a database transaction
    @Transactional
    public SubscriptionResponseDTO resumeSubscription(Long patientId, Long subscriptionId) {
        Subscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_SUBSCRIPTION, "id", subscriptionId));

        if (!sub.getPatient().getId().equals(patientId)) {
            throw new BadRequestException(MSG_UNAUTHORIZED);
        }

        if (!"PAUSED".equals(sub.getStatus())) {
            throw new BadRequestException("Only PAUSED subscriptions can be resumed.");
        }

        sub.setStatus("ACTIVE");
        Subscription saved = subscriptionRepository.save(sub);

        notificationService.createNotification(patientId, "Subscription Resumed", 
                MSG_SUB_FOR + sub.getMedicine().getMedicineName() + " is active again.", "SUCCESS");

        return convertToDTO(saved);
    }

    // Wraps execution inside a database transaction
    @Transactional
    public SubscriptionResponseDTO renewSubscription(Long patientId, Long subscriptionId, MultipartFile newPrescriptionFile) {
        Subscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_SUBSCRIPTION, "id", subscriptionId));

        if (!sub.getPatient().getId().equals(patientId)) {
            throw new BadRequestException(MSG_UNAUTHORIZED);
        }

        if (newPrescriptionFile != null && !newPrescriptionFile.isEmpty()) {
            Prescription newPrescription = prescriptionService.uploadPrescription(patientId, newPrescriptionFile, 6, null);
            sub.setPrescription(newPrescription);
        }

        sub.setStatus("PENDING"); // Requires admin review on renewal if updated
        Subscription saved = subscriptionRepository.save(sub);

        notificationService.createNotification(patientId, "Subscription Renewal Requested", 
                "Your renewal request for " + sub.getMedicine().getMedicineName() + " has been submitted for review.", "INFO");

        return convertToDTO(saved);
    }

    public int calculateDurationDays(int quantity, String dosage) {
        int dosagePerDay = 1;
        if (dosage != null) {
            Pattern pattern = Pattern.compile("(\\d+)");
            Matcher matcher = pattern.matcher(dosage);
            if (matcher.find()) {
                try {
                    int val = Integer.parseInt(matcher.group(1));
                    if (val > 0) dosagePerDay = val;
                } catch (Exception ignored) {
                    // Fallback to default dosage per day if parsing dosage string fails
                }
            }
        }
        int days = quantity / dosagePerDay;
        return Math.max(days, 1);
    }

    /**
     * Emergency 5-Day Bridge Supply:
     * Authorizes a 5-day emergency supply of chronic non-narcotic maintenance medication
     * to prevent clinical withdrawal when a doctor is unavailable to renew an expired prescription.
     */
    @Transactional
    public SubscriptionResponseDTO requestBridgeSupply(Long patientId, Long subscriptionId) {
        Subscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_SUBSCRIPTION, "id", subscriptionId));

        if (!sub.getPatient().getId().equals(patientId)) {
            throw new BadRequestException(MSG_UNAUTHORIZED);
        }

        if (Boolean.TRUE.equals(sub.getIsBridgeSupply())) {
            throw new BadRequestException("An emergency 5-day bridge supply has already been issued for this cycle. Please upload a renewed prescription.");
        }

        Medicine med = sub.getMedicine();
        if (med == null || med.getActive() == 0) {
            throw new BadRequestException("Assigned medication is inactive or unavailable for bridge dispensing.");
        }

        // 5 units emergency quantity
        int bridgeQty = 5;
        int deducted = medicineRepository.deductAvailableStock(med.getId(), bridgeQty);
        if (deducted == 0) {
            throw new InsufficientStockException("Insufficient warehouse stock to dispatch 5-day emergency bridge supply for " + med.getMedicineName());
        }
        med.setStockQuantity(med.getStockQuantity() - bridgeQty);

        // Compute pro-rated price
        BigDecimal unitPrice = med.getPrice();
        BigDecimal bridgeAmount = unitPrice.multiply(BigDecimal.valueOf(bridgeQty))
                .divide(BigDecimal.valueOf(Math.max(1, sub.getQuantity())), 2, RoundingMode.HALF_UP);
        if (bridgeAmount.compareTo(BigDecimal.valueOf(20)) < 0) {
            bridgeAmount = BigDecimal.valueOf(20);
        }

        // Create emergency bridge order
        Order bridgeOrder = new Order();
        bridgeOrder.setPatient(sub.getPatient());
        bridgeOrder.setSubscription(sub);
        bridgeOrder.setPrescription(sub.getPrescription());
        bridgeOrder.setIsBridgeSupply(true);
        bridgeOrder.setDispensingNotes("EMERGENCY 5-DAY BRIDGE SUPPLY: Authorized clinical stopgap pending physician renewal.");
        bridgeOrder.setDeliveryAddress(sub.getPatient().getAddress() != null ? sub.getPatient().getAddress() + ", " + sub.getPatient().getCity() : "Primary Address");
        bridgeOrder.setPaymentMethod("CASH_ON_DELIVERY");
        bridgeOrder.setPaymentStatus("PENDING");
        bridgeOrder.setOrderStatus("APPROVED");
        bridgeOrder.setOrderType("SUBSCRIPTION_REFILL");
        bridgeOrder.setOrderDate(LocalDateTime.now());
        bridgeOrder.setExpectedDeliveryDate(LocalDateTime.now().plusDays(2));
        bridgeOrder.setTotalAmount(bridgeAmount);

        OrderItem item = new OrderItem();
        item.setOrder(bridgeOrder);
        item.setMedicine(med);
        item.setQuantity(bridgeQty);
        item.setPrice(unitPrice);
        item.setSubtotal(bridgeAmount);
        bridgeOrder.getItems().add(item);

        orderRepository.save(bridgeOrder);

        // Mark subscription bridge supply active
        sub.setIsBridgeSupply(true);
        Subscription saved = subscriptionRepository.save(sub);

        notificationService.createNotification(
                patientId,
                "🚑 Emergency 5-Day Bridge Supply Approved",
                "An emergency 5-day stopgap supply of " + med.getMedicineName() + " has been authorized and dispatched (Order #" + bridgeOrder.getId() + "). Please schedule a doctor appointment to renew your prescription.",
                "SUCCESS"
        );

        notificationService.notifyStaff(
                "Clinical Notice: Emergency Bridge Dispatched",
                "Patient " + sub.getPatient().getName() + " was issued a 5-day bridge supply for " + med.getMedicineName() + " due to expired prescription. Order #" + bridgeOrder.getId() + ".",
                "INFO"
        );

        return convertToDTO(saved);
    }

    /**
     * Subscription Vacation Snooze:
     * Pauses/defers refill dispatch by 7 or 14 days and releases any soft-locked inventory.
     */
    @Transactional
    public SubscriptionResponseDTO snoozeSubscription(Long patientId, Long subscriptionId, int days) {
        Subscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_SUBSCRIPTION, "id", subscriptionId));

        if (!sub.getPatient().getId().equals(patientId)) {
            throw new BadRequestException(MSG_UNAUTHORIZED);
        }

        int snoozeDays = (days == 14) ? 14 : 7;

        // If inventory was reserved, release it back to general inventory pool while on vacation
        if ("RESERVED".equals(sub.getReservationStatus()) && sub.getMedicine() != null) {
            medicineRepository.releaseReservedStock(sub.getMedicine().getId(), sub.getQuantity());
            sub.setReservationStatus("NONE");
            sub.setReservationDate(null);
        }

        LocalDateTime baseDate = sub.getNextRefillDate() != null ? sub.getNextRefillDate() : LocalDateTime.now();
        LocalDateTime newNextRefill = baseDate.plusDays(snoozeDays);
        sub.setNextRefillDate(newNextRefill);
        sub.setNextDispatchDate(newNextRefill.minusDays(5));
        sub.setLastSnoozeDate(LocalDateTime.now());
        sub.setSnoozeCount(sub.getSnoozeCount() != null ? sub.getSnoozeCount() + 1 : 1);

        Subscription saved = subscriptionRepository.save(sub);

        notificationService.createNotification(
                patientId,
                "🏖️ Refill Snoozed for " + snoozeDays + " Days",
                "Your next refill for " + (sub.getMedicine() != null ? sub.getMedicine().getMedicineName() : "medication") + 
                " has been deferred by " + snoozeDays + " days. New expected refill date: " + newNextRefill.toLocalDate() + ". Enjoy your vacation!",
                "SUCCESS"
        );

        return convertToDTO(saved);
    }

    /**
     * Refill Synchronization ("Pillbox Day"):
     * Aligns all recurring subscription refill dates for a patient to a single preferred day of month.
     */
    @Transactional
    public List<SubscriptionResponseDTO> syncRefills(Long patientId, int preferredDayOfMonth) {
        int day = Math.min(28, Math.max(1, preferredDayOfMonth));
        List<Subscription> subs = subscriptionRepository.findByPatientIdOrderByCreatedAtDesc(patientId);

        LocalDateTime now = LocalDateTime.now();
        for (Subscription sub : subs) {
            if ("ACTIVE".equals(sub.getStatus())) {
                LocalDateTime targetDate = now.withDayOfMonth(day);
                if (targetDate.isBefore(now.plusDays(3))) {
                    targetDate = targetDate.plusMonths(1);
                }
                sub.setNextRefillDate(targetDate);
                sub.setNextDispatchDate(targetDate.minusDays(5));
                subscriptionRepository.save(sub);
            }
        }

        notificationService.createNotification(
                patientId,
                "📦 Refills Synchronized (Pillbox Day)",
                "All your recurring chronic medication refills have been synchronized to arrive together on the " + day + "th of each month in a single consolidated package.",
                "SUCCESS"
        );

        return subs.stream().map(this::convertToDTO).toList();
    }

    public SubscriptionResponseDTO convertToDTO(Subscription sub) {
        Prescription p = sub.getPrescription();
        Medicine m = sub.getMedicine();
        SubscriptionResponseDTO dto = new SubscriptionResponseDTO(
                sub.getId(),
                sub.getPatient().getId(),
                sub.getPatient().getName(),
                sub.getPatient().getEmail(),
                m != null ? m.getId() : null,
                m != null ? m.getMedicineName() : "Pending Pharmacist Review",
                m != null ? m.getBrandName() : "Pending Assignment",
                m != null ? m.getComposition() : "See Prescription",
                m != null ? m.getStrength() : "See Prescription",
                p != null ? p.getId() : null,
                p != null ? p.getFileName() : null,
                p != null ? p.getExpiryDate() : null,
                sub.getDosage(),
                sub.getFrequency(),
                sub.getQuantity(),
                sub.getStartDate(),
                sub.getNextRefillDate(),
                sub.getNextDispatchDate(),
                sub.getStatus(),
                sub.getReservationStatus(),
                sub.getReservationDate(),
                sub.getCreatedAt()
        );

        dto.setIsBridgeSupply(sub.getIsBridgeSupply());
        dto.setLastSnoozeDate(sub.getLastSnoozeDate());
        dto.setSnoozeCount(sub.getSnoozeCount());

        return dto;
    }
}
