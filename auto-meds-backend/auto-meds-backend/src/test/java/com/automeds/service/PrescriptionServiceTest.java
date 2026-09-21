package com.automeds.service;

import com.automeds.entity.Prescription;
import com.automeds.entity.User;
import com.automeds.exception.ResourceNotFoundException;
import com.automeds.repository.PrescriptionRepository;
import com.automeds.repository.UserRepository;
import com.automeds.util.FileStorageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class PrescriptionServiceTest {

    private PrescriptionRepository prescriptionRepository;
    private UserRepository userRepository;
    private FileStorageUtil fileStorageUtil;
    private PrescriptionService prescriptionService;

    @BeforeEach
    void setUp() {
        prescriptionRepository = Mockito.mock(PrescriptionRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        fileStorageUtil = Mockito.mock(FileStorageUtil.class);

        prescriptionService = new PrescriptionService(prescriptionRepository, userRepository, fileStorageUtil);
    }

    @Test
    void testUploadPrescriptionSuccess() {
        User u = new User();
        u.setId(1L);

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        Mockito.when(fileStorageUtil.storeFile(any())).thenReturn("uuid_file.pdf");

        Prescription saved = new Prescription();
        saved.setId(10L);
        saved.setFileName("file.pdf");
        saved.setFilePath("uuid_file.pdf");
        Mockito.when(prescriptionRepository.save(any(Prescription.class))).thenReturn(saved);

        MockMultipartFile file = new MockMultipartFile("file", "file.pdf", "application/pdf", "data".getBytes());
        Prescription result = prescriptionService.uploadPrescription(1L, file, 12, null);

        assertNotNull(result);
        assertEquals(10L, result.getId());
    }

    @Test
    void testGetPrescriptionByIdSuccess() {
        Prescription p = new Prescription();
        p.setId(1L);

        Mockito.when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(p));
        Prescription res = prescriptionService.getPrescriptionById(1L);
        assertEquals(1L, res.getId());
    }

    @Test
    void testGetPrescriptionByIdNotFound() {
        Mockito.when(prescriptionRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> prescriptionService.getPrescriptionById(99L));
    }

    @Test
    void testGetPrescriptionsForPatient() {
        Prescription p = new Prescription();
        p.setId(1L);

        Mockito.when(prescriptionRepository.findByPatientIdOrderByUploadDateDesc(1L)).thenReturn(Collections.singletonList(p));
        List<Prescription> list = prescriptionService.getPrescriptionsForPatient(1L);
        assertEquals(1, list.size());
    }
}


