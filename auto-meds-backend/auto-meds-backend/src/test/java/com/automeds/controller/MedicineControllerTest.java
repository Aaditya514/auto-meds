package com.automeds.controller;

import com.automeds.dto.MedicineDTO;
import com.automeds.service.MedicineService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MedicineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MedicineService medicineService;

    @Test
    void testGetAllMedicines() throws Exception {
        Mockito.when(medicineService.getAllActiveMedicines()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/medicines"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetMedicineById() throws Exception {
        MedicineDTO dto = new MedicineDTO();
        dto.setId(1L);
        Mockito.when(medicineService.getMedicineById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/medicines/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testSearchMedicines() throws Exception {
        Mockito.when(medicineService.searchMedicines("paracetamol")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/medicines/search").param("query", "paracetamol"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAlternativeMedicines() throws Exception {
        Mockito.when(medicineService.getAlternativeMedicines(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/medicines/1/alternatives"))
                .andExpect(status().isOk());
    }
}


