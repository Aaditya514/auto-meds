package com.automeds.service;

import com.automeds.dto.MedicineDTO;
import com.automeds.entity.Medicine;
import com.automeds.exception.ResourceNotFoundException;
import com.automeds.repository.MedicineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MedicineServiceTest {

    private MedicineRepository medicineRepository;
    private MedicineService medicineService;

    @BeforeEach
    void setUp() {
        medicineRepository = Mockito.mock(MedicineRepository.class);
        medicineService = new MedicineService(medicineRepository);
    }

    @Test
    void testGetAllActiveMedicines() {
        Medicine m = new Medicine();
        m.setId(1L);
        m.setActive(1);
        m.setRequiresPrescription(1);

        Mockito.when(medicineRepository.findByActive(1)).thenReturn(Collections.singletonList(m));

        List<MedicineDTO> list = medicineService.getAllActiveMedicines();
        assertEquals(1, list.size());
        assertTrue(list.get(0).getRequiresPrescription());
        assertTrue(list.get(0).getActive());
    }

    @Test
    void testGetMedicineByIdSuccess() {
        Medicine m = new Medicine();
        m.setId(1L);
        m.setActive(0);
        m.setRequiresPrescription(0);

        Mockito.when(medicineRepository.findById(1L)).thenReturn(Optional.of(m));

        MedicineDTO dto = medicineService.getMedicineById(1L);
        assertEquals(1L, dto.getId());
        assertFalse(dto.getRequiresPrescription());
        assertFalse(dto.getActive());
    }

    @Test
    void testGetMedicineByIdNotFound() {
        Mockito.when(medicineRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> medicineService.getMedicineById(99L));
    }

    @Test
    void testSearchMedicines() {
        Medicine m = new Medicine();
        m.setId(1L);

        Mockito.when(medicineRepository.searchMedicines("paracetamol")).thenReturn(Collections.singletonList(m));
        List<MedicineDTO> res1 = medicineService.searchMedicines("paracetamol");
        assertEquals(1, res1.size());

        Mockito.when(medicineRepository.findByActive(1)).thenReturn(Collections.singletonList(m));
        List<MedicineDTO> res2 = medicineService.searchMedicines("   ");
        assertEquals(1, res2.size());
    }

    @Test
    void testGetAlternativeMedicines() {
        Medicine original = new Medicine();
        original.setId(1L);
        original.setComposition("Metformin");
        original.setStrength("500mg");

        Medicine alt = new Medicine();
        alt.setId(2L);
        alt.setComposition("Metformin");
        alt.setStrength("500mg");

        Mockito.when(medicineRepository.findById(1L)).thenReturn(Optional.of(original));
        Mockito.when(medicineRepository.findAlternatives("Metformin", "500mg", 1L)).thenReturn(Collections.singletonList(alt));

        List<MedicineDTO> alternatives = medicineService.getAlternativeMedicines(1L);
        assertEquals(1, alternatives.size());
        assertEquals(2L, alternatives.get(0).getId());
    }
}


