package com.rfbooks.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rfbooks.config.AuthContext;
import com.rfbooks.dtos.IncomeImportRequest;
import com.rfbooks.dtos.IncomeImportResponse;
import com.rfbooks.services.IncomeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class IncomeImportIntegrationTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private IncomeService incomeService;

    @InjectMocks
    private IncomeController incomeController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(incomeController).build();
        AuthContext.setCurrentUserId("test-user");
    }

    @AfterEach
    void tearDown() {
        AuthContext.clear();
    }

    @Test
    void importIncome_returns200WithCounts() throws Exception {
        IncomeImportResponse response = new IncomeImportResponse();
        response.setCreated(2);
        response.setSkipped(1);
        when(incomeService.importIncome(any())).thenReturn(response);

        List<IncomeImportRequest> requests = List.of(
                makeRequest("res_1_pay_1", 250.00),
                makeRequest("res_1_pay_2", 100.00),
                makeRequest("res_2_pay_1", 500.00)
        );

        mockMvc.perform(post("/api/income/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.created").value(2))
                .andExpect(jsonPath("$.skipped").value(1))
                .andExpect(jsonPath("$.errors").value(0));
    }

    @Test
    void importIncome_returnsErrorDetails() throws Exception {
        IncomeImportResponse response = new IncomeImportResponse();
        response.setCreated(1);
        response.addError("Item 1: externalId is required");
        when(incomeService.importIncome(any())).thenReturn(response);

        List<IncomeImportRequest> requests = List.of(
                makeRequest("res_1_pay_1", 250.00),
                makeRequest(null, 100.00)
        );

        mockMvc.perform(post("/api/income/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.created").value(1))
                .andExpect(jsonPath("$.errors").value(1))
                .andExpect(jsonPath("$.errorDetails[0]").value("Item 1: externalId is required"));
    }

    @Test
    void importIncome_emptyArray() throws Exception {
        IncomeImportResponse response = new IncomeImportResponse();
        when(incomeService.importIncome(any())).thenReturn(response);

        mockMvc.perform(post("/api/income/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.created").value(0))
                .andExpect(jsonPath("$.skipped").value(0))
                .andExpect(jsonPath("$.errors").value(0));
    }

    private IncomeImportRequest makeRequest(String externalId, Double amount) {
        IncomeImportRequest req = new IncomeImportRequest();
        req.setExternalId(externalId);
        req.setAmount(amount);
        req.setIncomeDate(LocalDate.of(2026, 3, 15));
        req.setSource("Reservation #1");
        req.setCategory("Room Revenue");
        req.setPaymentMethod("card");
        return req;
    }
}
