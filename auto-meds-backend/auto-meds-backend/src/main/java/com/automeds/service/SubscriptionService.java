package com.automeds.service;

import com.automeds.dto.SubscriptionRequestDTO;
import com.automeds.dto.SubscriptionResponseDTO;
import com.automeds.entity.Medicine;
import com.automeds.entity.Prescription;
import com.automeds.entity.Subscription;
import com.automeds.entity.User;
import com.automeds.exception.BadRequestException;
import com.automeds.exception.ResourceNotFoundException;
import com.automeds.repository.MedicineRepository;

import com.automeds.repository.SubscriptionRepository;
import com.automeds.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private static final String ENTITY_SUBSCRIPTION = "Subscription";
    private static final String MSG_UNAUTHORIZED = "Unauthorized to modify this subscription.";
    private static final String MSG_SUB_FOR = "Your subscription for ";

    public SubscriptionService(SubscriptionRepository subscriptionRepository, MedicineRepository medicineRepository, UserRepository userRepository, PrescriptionService prescriptionService, NotificationService notificationService) {
        this.subscriptionRepository = subscriptionRepository;
        this.medicineRepository = medicineRepository;
        this.userRepository = userRepository;
        this.prescriptionService = prescriptionService;
        this.notificationService = notificationService;
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

    // Wraps execution inside a database transaction
    @Transactional
    public SubscriptionResponseDTO cancelSubscription(Long patientId, Long subscriptionId) {
        Subscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_SUBSCRIPTION, "id", subscriptionId));

        if (!sub.getPatient().getId().equals(patientId)) {
            throw new BadRequestException(MSG_UNAUTHORIZED);
        }

        sub.setStatus("CANCELLED");
        Subscription saved = subscriptionRepository.save(sub);

        notificationService.createNotification(patientId, "Subscription Cancelled", 
                MSG_SUB_FOR + sub.getMedicine().getMedicineName() + " has been cancelled.", "INFO");

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

        sub.setStatus("PAUSED");
        Subscription saved = subscriptionRepository.save(sub);

        notificationService.createNotification(patientId, "Subscription Paused", 
                MSG_SUB_FOR + sub.getMedicine().getMedicineName() + " has been paused.", "INFO");

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

    public SubscriptionResponseDTO convertToDTO(Subscription sub) {
        Prescription p = sub.getPrescription();
        Medicine m = sub.getMedicine();
        return new SubscriptionResponseDTO(
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
                sub.getCreatedAt()
        );
    }
}
