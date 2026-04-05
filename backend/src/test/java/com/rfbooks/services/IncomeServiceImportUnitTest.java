package com.rfbooks.services;

import com.rfbooks.config.AuthContext;
import com.rfbooks.dtos.IncomeImportRequest;
import com.rfbooks.dtos.IncomeImportResponse;
import com.rfbooks.entities.Income;
import com.rfbooks.repos.IncomeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncomeServiceImportUnitTest {

    @Mock
    private IncomeRepository incomeRepository;

    @InjectMocks
    private IncomeService incomeService;

    @BeforeEach
    void setUp() {
        AuthContext.setCurrentUserId("test-user");
    }

    @AfterEach
    void tearDown() {
        AuthContext.clear();
    }

    @Test
    void importIncome_createsNewRecords() {
        when(incomeRepository.findByUserIdAndExternalId(eq("test-user"), any()))
                .thenReturn(Optional.empty());
        when(incomeRepository.save(any(Income.class))).thenAnswer(inv -> inv.getArgument(0));

        IncomeImportRequest req = makeRequest("reservation_1_payment_1", 250.00, LocalDate.of(2026, 3, 15));

        IncomeImportResponse response = incomeService.importIncome(List.of(req));

        assertEquals(1, response.getCreated());
        assertEquals(0, response.getSkipped());
        assertEquals(0, response.getErrors());

        ArgumentCaptor<Income> captor = ArgumentCaptor.forClass(Income.class);
        verify(incomeRepository).save(captor.capture());
        Income saved = captor.getValue();
        assertEquals("reservation_1_payment_1", saved.getExternalId());
        assertEquals(250.00, saved.getAmount());
        assertEquals("test-user", saved.getUserId());
    }

    @Test
    void importIncome_skipsDuplicateExternalId() {
        Income existing = new Income();
        existing.setExternalId("reservation_1_payment_1");
        when(incomeRepository.findByUserIdAndExternalId("test-user", "reservation_1_payment_1"))
                .thenReturn(Optional.of(existing));

        IncomeImportRequest req = makeRequest("reservation_1_payment_1", 250.00, LocalDate.of(2026, 3, 15));

        IncomeImportResponse response = incomeService.importIncome(List.of(req));

        assertEquals(0, response.getCreated());
        assertEquals(1, response.getSkipped());
        assertEquals(0, response.getErrors());
        verify(incomeRepository, never()).save(any());
    }

    @Test
    void importIncome_handlesNewAndExistingMixed() {
        when(incomeRepository.findByUserIdAndExternalId("test-user", "reservation_1_payment_1"))
                .thenReturn(Optional.of(new Income()));
        when(incomeRepository.findByUserIdAndExternalId("test-user", "reservation_2_payment_3"))
                .thenReturn(Optional.empty());
        when(incomeRepository.save(any(Income.class))).thenAnswer(inv -> inv.getArgument(0));

        List<IncomeImportRequest> requests = List.of(
                makeRequest("reservation_1_payment_1", 250.00, LocalDate.of(2026, 3, 15)),
                makeRequest("reservation_2_payment_3", 180.50, LocalDate.of(2026, 3, 16))
        );

        IncomeImportResponse response = incomeService.importIncome(requests);

        assertEquals(1, response.getCreated());
        assertEquals(1, response.getSkipped());
        assertEquals(0, response.getErrors());
        verify(incomeRepository, times(1)).save(any());
    }

    @Test
    void importIncome_errorOnMissingExternalId() {
        IncomeImportRequest req = makeRequest(null, 100.00, LocalDate.of(2026, 3, 15));

        IncomeImportResponse response = incomeService.importIncome(List.of(req));

        assertEquals(0, response.getCreated());
        assertEquals(0, response.getSkipped());
        assertEquals(1, response.getErrors());
        assertTrue(response.getErrorDetails().get(0).contains("externalId is required"));
    }

    @Test
    void importIncome_errorOnMissingRequiredFields() {
        IncomeImportRequest req = makeRequest("ext_1", null, null);

        IncomeImportResponse response = incomeService.importIncome(List.of(req));

        assertEquals(0, response.getCreated());
        assertEquals(0, response.getSkipped());
        assertEquals(1, response.getErrors());
        assertTrue(response.getErrorDetails().get(0).contains("amount and incomeDate are required"));
    }

    @Test
    void importIncome_mapsAllFields() {
        when(incomeRepository.findByUserIdAndExternalId(eq("test-user"), any()))
                .thenReturn(Optional.empty());
        when(incomeRepository.save(any(Income.class))).thenAnswer(inv -> inv.getArgument(0));

        IncomeImportRequest req = makeRequest("ext_1", 500.00, LocalDate.of(2026, 4, 1));
        req.setSource("Reservation #42");
        req.setCategory("Room Revenue");
        req.setPaymentMethod("card");
        req.setReferenceNumber("REF-123");
        req.setDescription("Cabin rental payment");
        req.setNotes("Guest: John Doe");

        incomeService.importIncome(List.of(req));

        ArgumentCaptor<Income> captor = ArgumentCaptor.forClass(Income.class);
        verify(incomeRepository).save(captor.capture());
        Income saved = captor.getValue();
        assertEquals("Reservation #42", saved.getSource());
        assertEquals("Room Revenue", saved.getCategory());
        assertEquals("card", saved.getPaymentMethod());
        assertEquals("REF-123", saved.getReferenceNumber());
        assertEquals("Cabin rental payment", saved.getDescription());
        assertEquals("Guest: John Doe", saved.getNotes());
    }

    @Test
    void importIncome_emptyListReturnsZeroCounts() {
        IncomeImportResponse response = incomeService.importIncome(List.of());

        assertEquals(0, response.getCreated());
        assertEquals(0, response.getSkipped());
        assertEquals(0, response.getErrors());
    }

    private IncomeImportRequest makeRequest(String externalId, Double amount, LocalDate date) {
        IncomeImportRequest req = new IncomeImportRequest();
        req.setExternalId(externalId);
        req.setAmount(amount);
        req.setIncomeDate(date);
        req.setSource("Reservation #1");
        req.setCategory("Room Revenue");
        req.setPaymentMethod("card");
        return req;
    }
}
