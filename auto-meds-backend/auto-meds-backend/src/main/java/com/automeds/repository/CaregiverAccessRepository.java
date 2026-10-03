package com.automeds.repository;

import com.automeds.entity.CaregiverAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for CaregiverAccess entity.
 * Provides patient-centric and caregiver-centric access queries.
 */
@Repository
public interface CaregiverAccessRepository extends JpaRepository<CaregiverAccess, Long> {

    /**
     * Find all caregiver relationships for a given patient (those who can act on their behalf).
     */
    @Query("SELECT c FROM CaregiverAccess c JOIN FETCH c.caregiver WHERE c.patient.id = :patientId")
    List<CaregiverAccess> findAllByPatientId(@Param("patientId") Long patientId);

    /**
     * Find all patients that this caregiver is delegated to manage.
     */
    @Query("SELECT c FROM CaregiverAccess c JOIN FETCH c.patient WHERE c.caregiver.id = :caregiverId AND c.status = 'ACTIVE'")
    List<CaregiverAccess> findActiveDelegatedPatients(@Param("caregiverId") Long caregiverId);

    /**
     * Find a specific link between a patient and a caregiver.
     */
    @Query("SELECT c FROM CaregiverAccess c WHERE c.patient.id = :patientId AND c.caregiver.id = :caregiverId")
    Optional<CaregiverAccess> findByPatientIdAndCaregiverId(
            @Param("patientId") Long patientId,
            @Param("caregiverId") Long caregiverId);

    /**
     * Find all ACTIVE caregivers for a patient (used by notification fanout).
     */
    @Query("SELECT c FROM CaregiverAccess c JOIN FETCH c.caregiver WHERE c.patient.id = :patientId AND c.status = 'ACTIVE'")
    List<CaregiverAccess> findActiveCaregiversForPatient(@Param("patientId") Long patientId);

    /**
     * Verify whether a caregiver has active access to a specific patient.
     * Used as an authorization guard in proxy payment flows.
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM CaregiverAccess c " +
           "WHERE c.patient.id = :patientId AND c.caregiver.id = :caregiverId AND c.status = 'ACTIVE'")
    boolean existsActiveLink(@Param("patientId") Long patientId, @Param("caregiverId") Long caregiverId);
}
