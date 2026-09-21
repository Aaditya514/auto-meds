package com.automeds.repository;

import com.automeds.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: PrescriptionRepository
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findByPatientIdOrderByUploadDateDesc(Long patientId);
    List<Prescription> findByStatus(String status);
}
