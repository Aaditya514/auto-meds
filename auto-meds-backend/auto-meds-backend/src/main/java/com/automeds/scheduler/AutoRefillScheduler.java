package com.automeds.scheduler;

import com.automeds.entity.Medicine;
import com.automeds.entity.Order;
import com.automeds.entity.Prescription;
import com.automeds.entity.Subscription;
import com.automeds.repository.MedicineRepository;
import com.automeds.repository.SubscriptionRepository;
import com.automeds.service.NotificationService;
import com.automeds.service.OrderService;
import com.automeds.service.SmsService;
import com.automeds.service.SubscriptionService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@EnableScheduling
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: AutoRefillScheduler
 * Description: Distributed scheduler for chronic care auto-refills, 5-day pre-allocation inventory soft-locking,
 * and dual notification cadence for patients and clinical staff.
 */
public class AutoRefillScheduler {

    private static final Logger logger = LoggerFactory.getLogger(AutoRefillScheduler.class);

    private static final Set<Long> ALERT_DAYS_RX = Set.of(30L, 17L, 7L, 5L, 2L);
    private static final Set<Long> ALERT_DAYS_REFILL = Set.of(14L, 7L, 5L, 3L, 1L);

    private final SubscriptionRepository subscriptionRepository;
    private final MedicineRepository medicineRepository;
    private final OrderService orderService;
    private final SubscriptionService subscriptionService;
    private final NotificationService notificationService;
    private final SmsService smsService;

    @Value("${automeds.refill-buffer-days:5}")
    private int refillBufferDays;

    public AutoRefillScheduler(SubscriptionRepository subscriptionRepository, MedicineRepository medicineRepository, OrderService orderService, SubscriptionService subscriptionService, NotificationService notificationService, SmsService smsService) {
        this.subscriptionRepository = subscriptionRepository;
        this.medicineRepository = medicineRepository;
        this.orderService = orderService;
        this.subscriptionService = subscriptionService;
        this.notificationService = notificationService;
        this.smsService = smsService;
    }

    /**
     * Executes every day at midnight.
     * ShedLock guarantees that across multi-container instances, only ONE instance executes this task.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @SchedulerLock(name = "AutoRefillScheduler_processAutoRefills", lockAtLeastFor = "5m", lockAtMostFor = "30m")
    @Transactional
    public void processAutoRefills() {
        logger.info("Starting AutoRefillScheduler execution with ShedLock...");

        LocalDateTime now = LocalDateTime.now();

        // Step 1: 5-Day Inventory Soft-Lock Reservation Check
        LocalDateTime reservationThreshold = now.plusDays(refillBufferDays);
        List<Subscription> pendingReservations = subscriptionRepository.findPendingReservations("ACTIVE", reservationThreshold);
        logger.info("Found {} active subscriptions eligible for 5-day stock reservation.", pendingReservations.size());
        for (Subscription sub : pendingReservations) {
            try {
                reserveInventoryForSubscription(sub);
            } catch (Exception ex) {
                logger.error("Error reserving inventory for subscription id: " + sub.getId(), ex);
            }
        }

        // Step 2: Auto-Refill Order Execution for subscriptions due today
        List<Subscription> dueSubscriptions = subscriptionRepository.findByStatusAndNextRefillDateLessThanEqual("ACTIVE", now);
        logger.info("Found {} active subscriptions due for order generation.", dueSubscriptions.size());

        for (Subscription sub : dueSubscriptions) {
            try {
                processSingleSubscriptionRefill(sub);
            } catch (Exception ex) {
                logger.error("Error processing refill for subscription id: " + sub.getId(), ex);
            }
        }

        // Step 3: Dual Notification Cadence Watchers (Prescription Expiry & Upcoming Refill Depletion)
        try {
            sendPrescriptionExpiryAlerts();
            sendRefillApproachingAlerts();
        } catch (Exception ex) {
            logger.error("Error executing dual notification cadence alerts", ex);
        }

        logger.info("AutoRefillScheduler execution completed.");
    }

    /**
     * Dual Notification Cadence: Prescription Expiry Watcher (30, 17, 7, 5, 2 days).
     * Alerts both the patient and clinical pharmacists/admins.
     */
    public void sendPrescriptionExpiryAlerts() {
        List<Subscription> activeSubs = subscriptionRepository.findByStatus("ACTIVE");
        LocalDate today = LocalDate.now();

        for (Subscription sub : activeSubs) {
            Prescription rx = sub.getPrescription();
            Medicine medicine = sub.getMedicine();
            if (rx != null && rx.getExpiryDate() != null && medicine != null) {
                long daysLeft = ChronoUnit.DAYS.between(today, rx.getExpiryDate().toLocalDate());
                if (ALERT_DAYS_RX.contains(daysLeft)) {
                    String patientMsg = String.format(
                            "Your prescription for %s expires in %d days (%s). Please upload a renewed prescription to prevent auto-refill interruptions.",
                            medicine.getMedicineName(), daysLeft, rx.getExpiryDate().toLocalDate()
                    );
                    String staffMsg = String.format(
                            "Prescription for Patient %s (%s) expires in %d days (%s). Medicine: %s. Please review or follow up for renewal.",
                            sub.getPatient().getFullName(), sub.getPatient().getEmail(), daysLeft, rx.getExpiryDate().toLocalDate(), medicine.getMedicineName()
                    );
                    String severity = daysLeft <= 5 ? "DANGER" : "WARNING";
                    notificationService.notifyStaffAndPatient(
                            sub.getPatient().getId(),
                            "Prescription Renewal Due (" + daysLeft + " Days Left)",
                            patientMsg,
                            staffMsg,
                            severity
                    );
                }
            }
        }
    }

    /**
     * Dual Notification Cadence: Medication Refill Approaching Watcher (14, 7, 5, 3, 1 days).
     * Proactively reminds patient and warns pharmacy procurement of upcoming refill demands.
     */
    public void sendRefillApproachingAlerts() {
        List<Subscription> activeSubs = subscriptionRepository.findByStatus("ACTIVE");
        LocalDate today = LocalDate.now();

        for (Subscription sub : activeSubs) {
            Medicine medicine = sub.getMedicine();
            if (sub.getNextRefillDate() != null && medicine != null) {
                long daysLeft = ChronoUnit.DAYS.between(today, sub.getNextRefillDate().toLocalDate());
                if (ALERT_DAYS_REFILL.contains(daysLeft)) {
                    String patientMsg = String.format(
                            "Your upcoming chronic refill for %s (%d units) will dispatch in %d day(s) on %s. Please ensure your delivery address is accurate.",
                            medicine.getMedicineName(), sub.getQuantity(), daysLeft, sub.getNextRefillDate().toLocalDate()
                    );
                    int available = medicine.getAvailableQuantity();
                    String staffMsg;
                    String severity;
                    if (available < sub.getQuantity() && !"RESERVED".equals(sub.getReservationStatus())) {
                        severity = "DANGER";
                        staffMsg = String.format(
                                "⚠️ Refill Stockout Risk: Subscription #%d for %s (%s) due in %d day(s). Required: %d, Available: %d. Restock immediately!",
                                sub.getId(), sub.getPatient().getFullName(), sub.getPatient().getEmail(),
                                daysLeft, sub.getQuantity(), available
                        );
                    } else {
                        severity = "INFO";
                        staffMsg = String.format(
                                "Upcoming Refill: Subscription #%d for %s due in %d day(s). Quantity: %d units of %s. Stock is verified.",
                                sub.getId(), sub.getPatient().getFullName(), daysLeft, sub.getQuantity(), medicine.getMedicineName()
                        );
                    }
                    notificationService.notifyStaffAndPatient(
                            sub.getPatient().getId(),
                            "Upcoming Refill in " + daysLeft + " Day(s)",
                            patientMsg,
                            staffMsg,
                            severity
                    );
                }
            }
        }
    }

    /**
     * 5-Day Soft-Lock Reservation: Locks stock in the warehouse so ad-hoc buyers cannot deplete it.
     */
    public void reserveInventoryForSubscription(Subscription sub) {
        Medicine medicine = sub.getMedicine();
        if (medicine == null || medicine.getActive() == 0) {
            return;
        }

        int rows = medicineRepository.reserveStock(medicine.getId(), sub.getQuantity());
        if (rows > 0) {
            sub.setReservationStatus("RESERVED");
            sub.setReservationDate(LocalDateTime.now());
            subscriptionRepository.save(sub);

            notificationService.createNotification(
                    sub.getPatient().getId(),
                    "🔒 Refill Secured: Medication Locked in Vault",
                    "Your upcoming monthly refill of " + medicine.getMedicineName() + " (" + sub.getQuantity() + " units) has been locked and reserved in our pharmacy vault. Walk-in buyers cannot purchase your stock.",
                    "SUCCESS"
            );
            logger.info("Successfully soft-locked {} units of {} for subscription #{}", sub.getQuantity(), medicine.getMedicineName(), sub.getId());
        } else {
            sub.setReservationStatus("OUT_OF_STOCK_DEFICIT");
            subscriptionRepository.save(sub);

            notificationService.createNotification(
                    sub.getPatient().getId(),
                    "Refill Notice: Stock Replenishment in Progress",
                    "Your subscription refill for " + medicine.getMedicineName() + " is approaching in " + refillBufferDays + " days. Our pharmacy team has been alerted to prioritize restocking your medication.",
                    "WARNING"
            );

            int available = medicine.getAvailableQuantity();
            int deficit = sub.getQuantity() - available;
            String staffMsg = String.format(
                    "Stockout Warning! Subscription #%d for patient %s (%s) requires %d units of %s. Current available stock: %d. Deficit: %d units. Refill due in %d days. Please restock immediately.",
                    sub.getId(), sub.getPatient().getFullName(), sub.getPatient().getEmail(),
                    sub.getQuantity(), medicine.getMedicineName(), available,
                    Math.max(1, deficit), refillBufferDays
            );
            notificationService.notifyStaff(
                    "🚨 URGENT: Reorder Required for " + medicine.getMedicineName(),
                    staffMsg,
                    "DANGER"
            );
            logger.warn("Could not reserve {} units of {} for subscription #{}. Deficit recorded.", sub.getQuantity(), medicine.getMedicineName(), sub.getId());
        }
    }

    public void processSingleSubscriptionRefill(Subscription sub) {
        Prescription prescription = sub.getPrescription();
        Medicine medicine = sub.getMedicine();

        // 1. Check Prescription Expiration
        if (prescription != null && prescription.getExpiryDate() != null && prescription.getExpiryDate().isBefore(LocalDateTime.now())) {
            sub.setStatus("PRESCRIPTION_EXPIRED");
            subscriptionRepository.save(sub);

            notificationService.createNotification(
                    sub.getPatient().getId(),
                    "Prescription Expired — Refills Halted",
                    "Your prescription for " + medicine.getMedicineName() + " has EXPIRED on " + prescription.getExpiryDate().toLocalDate() + ". Automatic refill has been stopped. Please upload a new prescription.",
                    "DANGER"
            );
            return;
        }

        // 2. Check Stock & Generate Refill Order
        boolean isReserved = "RESERVED".equals(sub.getReservationStatus());
        boolean hasAvailableStock = medicine.getActive() == 1 && (isReserved || medicine.getAvailableQuantity() >= sub.getQuantity());

        if (hasAvailableStock) {
            // Generate Automatic Refill Order
            Order refillOrder = orderService.createSubscriptionRefillOrder(sub, medicine);

            // Recalculate next refill date
            int durationDays = subscriptionService.calculateDurationDays(sub.getQuantity(), sub.getDosage());
            LocalDateTime newNextRefill = LocalDateTime.now().plusDays(durationDays);
            sub.setNextRefillDate(newNextRefill);
            sub.setNextDispatchDate(newNextRefill.minusDays(refillBufferDays));
            sub.setReservationStatus("NONE");
            sub.setReservationDate(null);

            subscriptionRepository.save(sub);

            logger.info("Auto-refill order #{} created successfully for subscription #{}", refillOrder.getId(), sub.getId());

            // H1: SMS confirmation to patient on refill execution
            String patientPhone = sub.getPatient().getPhone();
            smsService.sendRefillConfirmationSms(
                    patientPhone,
                    sub.getPatient().getName() != null ? sub.getPatient().getName() : sub.getPatient().getFullName(),
                    medicine.getMedicineName(),
                    sub.getQuantity()
            );
        } else {
            // Out of Stock Handling -> Find Same Composition + Same Strength Alternatives
            logger.warn("Medicine #{} ({}) out of stock for subscription #{}. Searching alternatives...", medicine.getId(), medicine.getMedicineName(), sub.getId());

            List<Medicine> alternatives = medicineRepository.findAlternatives(
                    medicine.getComposition(),
                    medicine.getStrength(),
                    medicine.getId()
            ).stream().filter(m -> m.getAvailableQuantity() >= sub.getQuantity()).toList();

            if (!alternatives.isEmpty()) {
                String altNames = alternatives.stream()
                        .map(m -> m.getBrandName() + " (" + m.getMedicineName() + " - ₹" + m.getPrice() + ")")
                        .collect(Collectors.joining(", "));

                notificationService.createNotification(
                        sub.getPatient().getId(),
                        "Subscribed Medicine Out of Stock — Alternatives Available",
                        "Your subscribed medicine " + medicine.getMedicineName() + " (" + medicine.getBrandName() + ") is currently out of stock. Available in-stock alternatives with SAME composition & strength: " + altNames + ". Please log in to choose an alternative.",
                        "WARNING"
                );
                // H1: SMS stock alert to patient
                smsService.sendStockAlertSms(
                        sub.getPatient().getPhone(),
                        sub.getPatient().getName() != null ? sub.getPatient().getName() : sub.getPatient().getFullName(),
                        medicine.getMedicineName()
                );
            } else {
                notificationService.createNotification(
                        sub.getPatient().getId(),
                        "Subscribed Medicine Out of Stock",
                        "Your subscribed medicine " + medicine.getMedicineName() + " is currently out of stock. No exact composition + strength alternative is currently available. Our warehouse has been notified.",
                        "DANGER"
                );
            }
        }
    }
}
