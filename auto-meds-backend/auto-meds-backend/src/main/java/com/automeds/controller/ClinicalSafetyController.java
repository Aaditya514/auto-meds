package com.automeds.controller;

import com.automeds.dto.DrugInteractionAlertDTO;
import com.automeds.entity.Medicine;
import com.automeds.repository.MedicineRepository;
import com.automeds.service.DrugInteractionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/clinical", "/api/clinical-safety"})
public class ClinicalSafetyController {

    private final DrugInteractionService drugInteractionService;
    private final MedicineRepository medicineRepository;

    public ClinicalSafetyController(DrugInteractionService drugInteractionService,
                                    MedicineRepository medicineRepository) {
        this.drugInteractionService = drugInteractionService;
        this.medicineRepository = medicineRepository;
    }

    public static class InteractionCheckRequest {
        private List<Long> medicineIds;
        private String patientAllergies;
        private String patientConditions;

        public List<Long> getMedicineIds() {
            return medicineIds;
        }

        public void setMedicineIds(List<Long> medicineIds) {
            this.medicineIds = medicineIds;
        }

        public String getPatientAllergies() {
            return patientAllergies;
        }

        public void setPatientAllergies(String patientAllergies) {
            this.patientAllergies = patientAllergies;
        }

        public String getPatientConditions() {
            return patientConditions;
        }

        public void setPatientConditions(String patientConditions) {
            this.patientConditions = patientConditions;
        }
    }

    @PostMapping({"/check-interactions", "/check"})
    public ResponseEntity<List<DrugInteractionAlertDTO>> checkInteractions(
            @RequestBody InteractionCheckRequest request) {
        if (request.getMedicineIds() == null || request.getMedicineIds().isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<Medicine> meds = medicineRepository.findAllById(request.getMedicineIds());
        String profileInfo = (request.getPatientAllergies() != null ? request.getPatientAllergies() : "") +
                (request.getPatientConditions() != null ? " " + request.getPatientConditions() : "");
        List<DrugInteractionAlertDTO> alerts = drugInteractionService.checkInteractions(meds, profileInfo.trim());
        return ResponseEntity.ok(alerts);
    }
}
