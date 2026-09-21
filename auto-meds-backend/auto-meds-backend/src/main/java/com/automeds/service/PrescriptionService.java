package com.automeds.service;

import com.automeds.entity.Prescription;
import com.automeds.entity.User;
import com.automeds.exception.ResourceNotFoundException;
import com.automeds.repository.PrescriptionRepository;
import com.automeds.repository.UserRepository;
import com.automeds.util.FileStorageUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: PrescriptionService
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final UserRepository userRepository;
    private final FileStorageUtil fileStorageUtil;

    public PrescriptionService(PrescriptionRepository prescriptionRepository, UserRepository userRepository, FileStorageUtil fileStorageUtil) {
        this.prescriptionRepository = prescriptionRepository;
        this.userRepository = userRepository;
        this.fileStorageUtil = fileStorageUtil;
    }

    // Wraps execution inside a database transaction
    @Transactional
    public Prescription uploadPrescription(Long patientId, MultipartFile file, Integer expiryMonths, LocalDateTime doctorVisitDate) {
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", patientId));

        String storedFileName = fileStorageUtil.storeFile(file);

        Prescription prescription = new Prescription();
        prescription.setPatient(patient);
        prescription.setFileName(file.getOriginalFilename());
        prescription.setFilePath(storedFileName);
        prescription.setUploadDate(LocalDateTime.now());
        prescription.setDoctorVisitDate(doctorVisitDate);
        
        // Default prescription validity: 6 months or provided expiryMonths
        int months = (expiryMonths != null && expiryMonths > 0) ? expiryMonths : 6;
        LocalDateTime baseDate = (doctorVisitDate != null) ? doctorVisitDate : LocalDateTime.now();
        prescription.setExpiryDate(baseDate.plusMonths(months));
        prescription.setStatus("PENDING");

        return prescriptionRepository.save(prescription);
    }

    // Wraps execution inside a database transaction
    @Transactional(readOnly = true)
    public Prescription getPrescriptionById(Long id) {
        return prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", "id", id));
    }

    // Wraps execution inside a database transaction
    @Transactional(readOnly = true)
    public List<Prescription> getPrescriptionsForPatient(Long patientId) {
        return prescriptionRepository.findByPatientIdOrderByUploadDateDesc(patientId);
    }
}
