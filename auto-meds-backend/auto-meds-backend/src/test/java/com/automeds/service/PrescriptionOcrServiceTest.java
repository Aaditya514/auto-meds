package com.automeds.service;

import com.automeds.dto.PrescriptionOcrCandidateDTO;
import com.automeds.dto.PrescriptionOcrDTO;
import com.automeds.entity.Medicine;
import com.automeds.repository.MedicineRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class PrescriptionOcrServiceTest {

    private MedicineRepository medicineRepository;
    private ObjectMapper objectMapper;
    private PrescriptionOcrService ocrService;

    private Medicine metformin;
    private Medicine amlodipine;
    private Medicine genericMetformin;

    @BeforeEach
    void setUp() {
        medicineRepository = Mockito.mock(MedicineRepository.class);
        objectMapper = new ObjectMapper();
        ocrService = new PrescriptionOcrService(medicineRepository, objectMapper);

        metformin = new Medicine();
        metformin.setId(10L);
        metformin.setMedicineName("Metformin 500mg Tablet");
        metformin.setBrandName("Glycomet");
        metformin.setComposition("Metformin Hydrochloride");
        metformin.setStrength("500mg");
        metformin.setPrice(new BigDecimal("65.00"));
        metformin.setActive(1);

        genericMetformin = new Medicine();
        genericMetformin.setId(11L);
        genericMetformin.setMedicineName("Metformin Generic 500mg");
        genericMetformin.setBrandName("Jan Aushadhi");
        genericMetformin.setComposition("Metformin Hydrochloride");
        genericMetformin.setStrength("500mg");
        genericMetformin.setPrice(new BigDecimal("22.00"));
        genericMetformin.setActive(1);

        amlodipine = new Medicine();
        amlodipine.setId(20L);
        amlodipine.setMedicineName("Amlodipine 5mg");
        amlodipine.setBrandName("Amlokind");
        amlodipine.setComposition("Amlodipine Besylate");
        amlodipine.setStrength("5mg");
        amlodipine.setPrice(new BigDecimal("40.00"));
        amlodipine.setActive(1);

        when(medicineRepository.findByActive(eq(1))).thenReturn(List.of(metformin, genericMetformin, amlodipine));
    }

    @Test
    void testProcessPrescriptionWithDoctorAndMedicines() {
        String prescriptionContent = "PRESCRIPTION\n" +
                "Doctor: Dr. Rajesh Gupta, MD (Cardiology)\n" +
                "Registration No: MCI-99482\n" +
                "Clinic: Heart & Diabetes Care Center\n" +
                "Date: 12/09/2026\n" +
                "Rx Lines:\n" +
                "1. Tab Metformin 500mg - 1-0-1 PC x 30 days\n" +
                "2. Tab Amlodipine 5mg - 1-0-0 x 30 days\n";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "rx_scan.txt",
                "text/plain",
                prescriptionContent.getBytes()
        );

        PrescriptionOcrDTO dto = ocrService.processPrescription(file);

        assertNotNull(dto);
        assertEquals("Dr. Rajesh Gupta", dto.getDoctorName());
        assertEquals("MCI-99482", dto.getDoctorRegNumber());
        assertTrue(dto.getConfidenceOverall() >= 0.70);
        assertFalse(dto.getCandidates().isEmpty());

        // Check candidate 1 (Metformin)
        PrescriptionOcrCandidateDTO c1 = dto.getCandidates().stream()
                .filter(c -> c.getMedicineName().contains("Metformin"))
                .findFirst().orElse(null);

        assertNotNull(c1);
        assertEquals("Twice Daily", c1.getFrequency());
        assertEquals(30, c1.getQuantity());
        assertNotNull(c1.getGenericAlternative());
        assertTrue(c1.getSavingsPercentage() > 0); // Cost savings with generic equivalent
    }

    @Test
    void testSerializationAndDeserialization() {
        PrescriptionOcrDTO dto = new PrescriptionOcrDTO();
        dto.setDoctorName("Dr. Sarah Lee");
        dto.setDoctorRegNumber("MCI-12345");
        dto.setConfidenceOverall(0.92);

        String json = ocrService.toJson(dto);
        assertNotNull(json);
        assertTrue(json.contains("Dr. Sarah Lee"));

        PrescriptionOcrDTO parsed = ocrService.fromJson(json);
        assertNotNull(parsed);
        assertEquals("Dr. Sarah Lee", parsed.getDoctorName());
        assertEquals("MCI-12345", parsed.getDoctorRegNumber());
        assertEquals(0.92, parsed.getConfidenceOverall());
    }
}
