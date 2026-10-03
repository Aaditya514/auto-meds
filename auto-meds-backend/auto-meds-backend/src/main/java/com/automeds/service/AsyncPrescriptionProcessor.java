package com.automeds.service;

import com.automeds.dto.PrescriptionOcrDTO;
import com.automeds.entity.Prescription;
import com.automeds.repository.PrescriptionRepository;
import com.automeds.util.FileStorageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: AsyncPrescriptionProcessor
 * Description: Non-blocking asynchronous OCR execution worker (P0).
 * Decouples heavy CPU document scanning and image rendering from the main HTTP servlet threads,
 * pushing completion notifications via Server-Sent Events (P4) directly to active user sessions.
 */
public class AsyncPrescriptionProcessor {

    private static final Logger log = LoggerFactory.getLogger(AsyncPrescriptionProcessor.class);

    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionOcrService prescriptionOcrService;
    private final FileStorageUtil fileStorageUtil;
    private final SseNotificationService sseNotificationService;

    public AsyncPrescriptionProcessor(
            PrescriptionRepository prescriptionRepository,
            PrescriptionOcrService prescriptionOcrService,
            FileStorageUtil fileStorageUtil,
            SseNotificationService sseNotificationService) {
        this.prescriptionRepository = prescriptionRepository;
        this.prescriptionOcrService = prescriptionOcrService;
        this.fileStorageUtil = fileStorageUtil;
        this.sseNotificationService = sseNotificationService;
    }

    @Async("ocrTaskExecutor")
    @Transactional
    public CompletableFuture<Void> processOcrAsync(Long prescriptionId, String storedFileName, String originalFilename) {
        log.info("Async OCR Worker started processing prescription #{} (file: {})", prescriptionId, storedFileName);

        try {
            Prescription prescription = prescriptionRepository.findById(prescriptionId).orElse(null);
            if (prescription == null) {
                log.warn("Prescription #{} not found during async OCR dispatch", prescriptionId);
                return CompletableFuture.completedFuture(null);
            }

            Path filePath = fileStorageUtil.getFilePath(storedFileName);
            PrescriptionOcrDTO ocrResult = prescriptionOcrService.processPrescriptionPath(filePath, originalFilename);

            prescription.setOcrData(prescriptionOcrService.toJson(ocrResult));
            prescription.setStatus("PENDING");
            prescriptionRepository.save(prescription);

            log.info("Async OCR completed for prescription #{}. Detected {} candidate medicines (Confidence: {})",
                    prescriptionId,
                    ocrResult.getCandidates() != null ? ocrResult.getCandidates().size() : 0,
                    ocrResult.getConfidenceOverall());

            Long patientId = prescription.getPatient() != null ? prescription.getPatient().getId() : null;

            // P4: Real-time SSE push to patient
            if (patientId != null) {
                sseNotificationService.sendToPatient(patientId, "OCR_COMPLETED", Map.of(
                        "prescriptionId", prescriptionId,
                        "fileName", originalFilename,
                        "candidatesCount", ocrResult.getCandidates() != null ? ocrResult.getCandidates().size() : 0,
                        "confidenceOverall", ocrResult.getConfidenceOverall(),
                        "message", "AI Optical Prescription analysis completed."
                ));
            }

            // P4: Real-time SSE push to clinical pharmacist staff
            sseNotificationService.sendToStaff("PRESCRIPTION_READY_FOR_TRIAGE", Map.of(
                    "prescriptionId", prescriptionId,
                    "patientName", prescription.getPatient() != null ? prescription.getPatient().getFullName() : "Patient",
                    "candidatesCount", ocrResult.getCandidates() != null ? ocrResult.getCandidates().size() : 0,
                    "message", "New prescription OCR verified and queued for pharmacist audit."
            ));

        } catch (Exception ex) {
            log.error("Asynchronous OCR task failed for prescription #" + prescriptionId, ex);
        }

        return CompletableFuture.completedFuture(null);
    }
}
