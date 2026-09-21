package com.automeds.service;

import com.automeds.dto.MedicineDTO;
import com.automeds.entity.Medicine;
import com.automeds.exception.ResourceNotFoundException;
import com.automeds.repository.MedicineRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: MedicineService
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public class MedicineService {

    private final MedicineRepository medicineRepository;

    public MedicineService(MedicineRepository medicineRepository) {
        this.medicineRepository = medicineRepository;
    }

    public List<MedicineDTO> getAllActiveMedicines() {
        return medicineRepository.findByActive(1).stream()
                .map(this::convertToDTO)
                .toList();
    }

    public MedicineDTO getMedicineById(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine", "id", id));
        return convertToDTO(medicine);
    }

    public List<MedicineDTO> searchMedicines(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllActiveMedicines();
        }
        return medicineRepository.searchMedicines(query.trim()).stream()
                .map(this::convertToDTO)
                .toList();
    }

    /**
     * Alternative Medicine Resolution:
     * Finds medicines matching EXACT SAME composition AND EXACT SAME strength, excluding original medicineId.
     */
    public List<MedicineDTO> getAlternativeMedicines(Long medicineId) {
        Medicine originalMedicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine", "id", medicineId));

        List<Medicine> alternatives = medicineRepository.findAlternatives(
                originalMedicine.getComposition(),
                originalMedicine.getStrength(),
                medicineId
        );

        return alternatives.stream()
                .map(this::convertToDTO)
                .toList();
    }

    public MedicineDTO convertToDTO(Medicine medicine) {
        return new MedicineDTO(
                medicine.getId(),
                medicine.getMedicineName(),
                medicine.getBrandName(),
                medicine.getComposition(),
                medicine.getStrength(),
                medicine.getCategory(),
                medicine.getPrice(),
                medicine.getStockQuantity(),
                medicine.getRequiresPrescription() != null && medicine.getRequiresPrescription() == 1,
                medicine.getDescription(),
                medicine.getManufacturer(),
                medicine.getExpiryDate(),
                medicine.getActive() != null && medicine.getActive() == 1
        );
    }
}
