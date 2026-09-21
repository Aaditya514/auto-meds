package com.automeds.controller;

import com.automeds.dto.MedicineDTO;
import com.automeds.service.MedicineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicines")
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: MedicineController
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public class MedicineController {

    private final MedicineService medicineService;

    public MedicineController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    @GetMapping
    public ResponseEntity<List<MedicineDTO>> getAllMedicines() {
        return ResponseEntity.ok(medicineService.getAllActiveMedicines());
    }

    // Handles GET requests at this endpoint
    @GetMapping("/{id}")
    public ResponseEntity<MedicineDTO> getMedicineById(@PathVariable Long id) {
        return ResponseEntity.ok(medicineService.getMedicineById(id));
    }

    // Handles GET requests at this endpoint
    @GetMapping("/search")
    public ResponseEntity<List<MedicineDTO>> searchMedicines(@RequestParam(required = false, defaultValue = "") String query) {
        return ResponseEntity.ok(medicineService.searchMedicines(query));
    }

    /**
     * Finds alternative medicine brands matching SAME composition AND SAME strength
     */
    // Handles GET requests at this endpoint
    @GetMapping("/{id}/alternatives")
    public ResponseEntity<List<MedicineDTO>> getAlternativeMedicines(@PathVariable Long id) {
        return ResponseEntity.ok(medicineService.getAlternativeMedicines(id));
    }
}
