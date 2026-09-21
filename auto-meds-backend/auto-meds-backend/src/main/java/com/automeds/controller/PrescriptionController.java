package com.automeds.controller;

import com.automeds.entity.Prescription;
import com.automeds.exception.BadRequestException;
import com.automeds.security.UserPrincipal;
import com.automeds.service.PrescriptionService;
import com.automeds.util.FileStorageUtil;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: PrescriptionController
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final FileStorageUtil fileStorageUtil;

    public PrescriptionController(PrescriptionService prescriptionService, FileStorageUtil fileStorageUtil) {
        this.prescriptionService = prescriptionService;
        this.fileStorageUtil = fileStorageUtil;
    }

    // Handles GET requests at this endpoint
    @GetMapping("/my")
    public ResponseEntity<List<Prescription>> getMyPrescriptions(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(prescriptionService.getPrescriptionsForPatient(userPrincipal.getId()));
    }

    // Handles GET requests at this endpoint
    @GetMapping("/{id}")
    public ResponseEntity<Resource> downloadPrescription(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {

        Prescription prescription = prescriptionService.getPrescriptionById(id);

        boolean isAdmin = userPrincipal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN"));

        if (!isAdmin && !prescription.getPatient().getId().equals(userPrincipal.getId())) {
            throw new BadRequestException("Unauthorized access to prescription file.");
        }

        try {
            String rawPath = prescription.getFilePath();
            Path filePath = fileStorageUtil.getFilePath(rawPath);

            if (!Files.exists(filePath)) {
                Path directPath = Paths.get(rawPath);
                if (Files.exists(directPath)) {
                    filePath = directPath;
                } else if (directPath.getFileName() != null) {
                    filePath = fileStorageUtil.getFilePath(directPath.getFileName().toString());
                }
            }

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new BadRequestException("File not found on server.");
            }

            String contentType = "application/octet-stream";
            if (prescription.getFileName().endsWith(".pdf")) {
                contentType = "application/pdf";
            } else if (prescription.getFileName().endsWith(".png")) {
                contentType = "image/png";
            } else if (prescription.getFileName().endsWith(".jpg") || prescription.getFileName().endsWith(".jpeg")) {
                contentType = "image/jpeg";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + prescription.getFileName() + "\"")
                    .body(resource);

        } catch (Exception ex) {
            throw new BadRequestException("Could not read prescription file.");
        }
    }
}
