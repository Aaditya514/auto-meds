package com.automeds.scheduler;

import com.automeds.entity.Medicine;
import com.automeds.entity.Order;
import com.automeds.entity.Prescription;
import com.automeds.entity.Subscription;
import com.automeds.repository.MedicineRepository;
import com.automeds.repository.SubscriptionRepository;
import com.automeds.service.NotificationService;
import com.automeds.service.OrderService;
import com.automeds.service.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@EnableScheduling
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: AutoRefillScheduler
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public class AutoRefillScheduler {

    private static final Logger logger = LoggerFactory.getLogger(AutoRefillScheduler.class);

    private final SubscriptionRepository subscriptionRepository;
    private final MedicineRepository medicineRepository;
    private final OrderService orderService;
    private final SubscriptionService subscriptionService;
    private final NotificationService notificationService;

    @Value("${automeds.refill-buffer-days:5}")
    private int refillBufferDays;

    public AutoRefillScheduler(SubscriptionRepository subscriptionRepository, MedicineRepository medicineRepository, OrderService orderService, SubscriptionService subscriptionService, NotificationService notificationService) {
        this.subscriptionRepository = subscriptionRepository;
        this.medicineRepository = medicineRepository;
        this.orderService = orderService;
        this.subscriptionService = subscriptionService;
        this.notificationService = notificationService;
    }

    /**
     * Executes every day at midnight (or configured fixed rate).
     */
    @Scheduled(cron = "0 0 0 * * ?")
    // Wraps execution inside a database transaction
    @Transactional
    public void processAutoRefills() {
        logger.info("Starting AutoRefillScheduler execution...");

        LocalDateTime bufferThreshold = LocalDateTime.now().plusDays(refillBufferDays);
        List<Subscription> activeSubscriptions = subscriptionRepository.findByStatusAndNextRefillDateLessThanEqual("ACTIVE", bufferThreshold);

        logger.info("Found {} active subscriptions approaching refill date.", activeSubscriptions.size());

        for (Subscription sub : activeSubscriptions) {
            try {
                processSingleSubscriptionRefill(sub);
            } catch (Exception ex) {
                logger.error("Error processing refill for subscription id: " + sub.getId(), ex);
            }
        }

        logger.info("AutoRefillScheduler execution completed.");
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

        // Advance prescription expiry warnings (30, 15, 7 days)
        if (prescription != null && prescription.getExpiryDate() != null) {
            long daysUntilExpiry = java.time.Duration.between(LocalDateTime.now(), prescription.getExpiryDate()).toDays();
            if (daysUntilExpiry == 30 || daysUntilExpiry == 15 || daysUntilExpiry == 7) {
                notificationService.createNotification(
                        sub.getPatient().getId(),
                        "Prescription Expiring Soon",
                        "Your prescription for " + medicine.getMedicineName() + " will expire in " + daysUntilExpiry + " days (" + prescription.getExpiryDate().toLocalDate() + "). Please prepare a renewal.",
                        "WARNING"
                );
            }
        }

        // 2. Check Medicine Stock
        if (medicine.getActive() == 1 && medicine.getStockQuantity() >= sub.getQuantity()) {
            // Generate Automatic Refill Order
            Order refillOrder = orderService.createSubscriptionRefillOrder(sub, medicine);

            // Recalculate next refill date
            int durationDays = subscriptionService.calculateDurationDays(sub.getQuantity(), sub.getDosage());
            LocalDateTime newNextRefill = LocalDateTime.now().plusDays(durationDays);
            sub.setNextRefillDate(newNextRefill);
            sub.setNextDispatchDate(newNextRefill.minusDays(refillBufferDays));

            subscriptionRepository.save(sub);

            logger.info("Auto-refill order #{} created successfully for subscription #{}", refillOrder.getId(), sub.getId());
        } else {
            // Out of Stock Handling -> Find Same Composition + Same Strength Alternatives
            logger.warn("Medicine #{} ({}) out of stock for subscription #{}. Searching alternatives...", medicine.getId(), medicine.getMedicineName(), sub.getId());

            List<Medicine> alternatives = medicineRepository.findAlternatives(
                    medicine.getComposition(),
                    medicine.getStrength(),
                    medicine.getId()
            ).stream().filter(m -> m.getStockQuantity() >= sub.getQuantity()).toList();

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
