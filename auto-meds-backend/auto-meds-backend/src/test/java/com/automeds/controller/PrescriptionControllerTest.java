package com.automeds.controller;

import com.automeds.entity.Prescription;
import com.automeds.entity.User;
import com.automeds.security.UserPrincipal;
import com.automeds.service.PrescriptionService;
import com.automeds.util.FileStorageUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PrescriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PrescriptionService prescriptionService;

    @MockBean
    private FileStorageUtil fileStorageUtil;

    @TempDir
    Path tempDir;

    private UserPrincipal mockPatient() {
        return new UserPrincipal(1L, "Patient", "patient@test.com", "pass", Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_PATIENT")));
    }


    @Test
    void testGetMyPrescriptions() throws Exception {
        Mockito.when(prescriptionService.getPrescriptionsForPatient(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/prescriptions/my").with(user(mockPatient())))
                .andExpect(status().isOk());
    }

    @Test
    void testDownloadPrescriptionSuccess() throws Exception {
        User patientUser = new User();
        patientUser.setId(1L);

        File dummyFile = tempDir.resolve("sample.pdf").toFile();
        dummyFile.createNewFile();

        Prescription p = new Prescription();
        p.setId(10L);
        p.setPatient(patientUser);
        p.setFileName("sample.pdf");
        p.setFilePath(dummyFile.getAbsolutePath());

        Mockito.when(prescriptionService.getPrescriptionById(10L)).thenReturn(p);
        Mockito.when(fileStorageUtil.getFilePath(dummyFile.getAbsolutePath())).thenReturn(dummyFile.toPath());

        mockMvc.perform(get("/api/prescriptions/10").with(user(mockPatient())))
                .andExpect(status().isOk());
    }

    @Test
    void testDownloadPrescriptionUnauthorized() throws Exception {
        User patientUser = new User();
        patientUser.setId(2L); // Different patient

        Prescription p = new Prescription();
        p.setId(10L);
        p.setPatient(patientUser);

        Mockito.when(prescriptionService.getPrescriptionById(10L)).thenReturn(p);

        mockMvc.perform(get("/api/prescriptions/10").with(user(mockPatient())))
                .andExpect(status().isBadRequest());
    }
}


