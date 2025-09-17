package com.freightops.service;

import com.freightops.entity.LTA;
import com.freightops.entity.City;
import com.freightops.entity.Client;
import com.freightops.dto.LTARequest;
import com.freightops.enums.LTAStatus;
import com.freightops.enums.PaymentMode;
import com.freightops.repository.LTARepository;
import com.freightops.repository.CityRepository;
import com.freightops.repository.ClientRepository;
import com.freightops.repository.TariffRepository;
import com.freightops.accounting.service.AccountService;
import com.freightops.accounting.service.FiscalYearService;
import com.freightops.accounting.service.JournalEntryService;
import com.freightops.accounting.entity.Account;
import com.freightops.accounting.entity.FiscalYear;
import com.freightops.accounting.entity.JournalEntry;
import com.freightops.accounting.enums.AccountType;
import com.freightops.accounting.enums.SourceType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LTAServiceTest {

    @Mock
    private LTARepository ltaRepository;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private TariffRepository tariffRepository;

    @Mock
    private AccountService accountService;

    @Mock
    private FiscalYearService fiscalYearService;

    @Mock
    private JournalEntryService journalEntryService;

    @InjectMocks
    private LTAService ltaService;

    private LTARequest ltaRequest;
    private City originCity;
    private City destinationCity;
    private Client client;
    private FiscalYear fiscalYear;
    private Account clientAccount;
    private Account salesAccount;

    @BeforeEach
    void setUp() {
        // Setup test data
        originCity = new City();
        originCity.setId(1L);
        originCity.setName("Douala");

        destinationCity = new City();
        destinationCity.setId(2L);
        destinationCity.setName("Yaoundé");

        client = new Client();
        client.setId(1L);
        client.setName("Test Client");

        fiscalYear = new FiscalYear();
        fiscalYear.setId(1L);
        fiscalYear.setStartDate(LocalDate.of(2024, 1, 1));
        fiscalYear.setEndDate(LocalDate.of(2024, 12, 31));

        clientAccount = new Account();
        clientAccount.setId(1L);
        clientAccount.setAccountNumber("411");
        clientAccount.setAccountName("Clients");
        clientAccount.setAccountType(AccountType.ASSET);

        salesAccount = new Account();
        salesAccount.setId(2L);
        salesAccount.setAccountNumber("701");
        salesAccount.setAccountName("Ventes de transport");
        salesAccount.setAccountType(AccountType.REVENUE);

        ltaRequest = new LTARequest();
        ltaRequest.setOriginCityId(1L);
        ltaRequest.setDestinationCityId(2L);
        ltaRequest.setClientId(1L);
        ltaRequest.setShipperName("Test Shipper");
        ltaRequest.setConsigneeName("Test Consignee");
        ltaRequest.setTotalWeight(new BigDecimal("10.5"));
        ltaRequest.setPaymentMode(PaymentMode.CASH);
        ltaRequest.setPackageNature("Electronics");
        ltaRequest.setPackageCount(2);
    }

    @Test
    void testCreateLTA_ShouldGenerateAccountingEntries() {
        // Given
        LTA savedLTA = new LTA();
        savedLTA.setId(1L);
        savedLTA.setLtaNumber("LTA-TEST-001");
        savedLTA.setOriginCity(originCity);
        savedLTA.setDestinationCity(destinationCity);
        savedLTA.setClient(client);
        savedLTA.setCalculatedCost(new BigDecimal("21.0")); // 10.5 kg * 2.0 default rate

        when(cityRepository.findById(1L)).thenReturn(Optional.of(originCity));
        when(cityRepository.findById(2L)).thenReturn(Optional.of(destinationCity));
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(ltaRepository.save(any(LTA.class))).thenReturn(savedLTA);
        when(ltaRepository.existsByLtaNumber(anyString())).thenReturn(false);
        when(fiscalYearService.getCurrentFiscalYear()).thenReturn(fiscalYear);
        when(accountService.getAccountByNumber("411")).thenReturn(clientAccount);
        when(accountService.getAccountByNumber("701")).thenReturn(salesAccount);

        // When
        LTA result = ltaService.createLTA(ltaRequest);

        // Then
        assertNotNull(result);
        assertEquals("LTA-TEST-001", result.getLtaNumber());
        assertEquals(new BigDecimal("21.0"), result.getCalculatedCost());

        // Verify accounting integration was called
        verify(fiscalYearService).getCurrentFiscalYear();
        verify(accountService).getAccountByNumber("411");
        verify(accountService).getAccountByNumber("701");
        verify(journalEntryService).saveJournalEntry(any(JournalEntry.class));
    }

    @Test
    void testCreateLTA_ShouldHandleAccountingErrorGracefully() {
        // Given
        LTA savedLTA = new LTA();
        savedLTA.setId(1L);
        savedLTA.setLtaNumber("LTA-TEST-002");
        savedLTA.setCalculatedCost(new BigDecimal("21.0"));

        when(cityRepository.findById(1L)).thenReturn(Optional.of(originCity));
        when(cityRepository.findById(2L)).thenReturn(Optional.of(destinationCity));
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(ltaRepository.save(any(LTA.class))).thenReturn(savedLTA);
        when(ltaRepository.existsByLtaNumber(anyString())).thenReturn(false);
        when(fiscalYearService.getCurrentFiscalYear()).thenReturn(null); // Simulate no fiscal year

        // When
        LTA result = ltaService.createLTA(ltaRequest);

        // Then
        assertNotNull(result);
        assertEquals("LTA-TEST-002", result.getLtaNumber());

        // Verify LTA creation succeeded despite accounting error
        verify(ltaRepository).save(any(LTA.class));
        verify(journalEntryService, never()).saveJournalEntry(any(JournalEntry.class));
    }

    @Test
    void testCreateLTA_ShouldCalculateCostCorrectly() {
        // Given
        LTA savedLTA = new LTA();
        savedLTA.setId(1L);
        savedLTA.setCalculatedCost(new BigDecimal("21.0"));

        when(cityRepository.findById(1L)).thenReturn(Optional.of(originCity));
        when(cityRepository.findById(2L)).thenReturn(Optional.of(destinationCity));
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(ltaRepository.save(any(LTA.class))).thenReturn(savedLTA);
        when(ltaRepository.existsByLtaNumber(anyString())).thenReturn(false);
        when(fiscalYearService.getCurrentFiscalYear()).thenReturn(fiscalYear);
        when(accountService.getAccountByNumber("411")).thenReturn(clientAccount);
        when(accountService.getAccountByNumber("701")).thenReturn(salesAccount);

        // When
        LTA result = ltaService.createLTA(ltaRequest);

        // Then
        verify(ltaRepository).save(argThat(lta -> lta.getCalculatedCost().equals(new BigDecimal("21.0")) // 10.5 * 2.0
                                                                                                         // default rate
        ));
    }

    @Test
    void testAccountingIntegration_ShouldCreateCorrectJournalEntries() {
        // Given
        LTA savedLTA = new LTA();
        savedLTA.setId(1L);
        savedLTA.setLtaNumber("LTA-TEST-003");
        savedLTA.setClient(client);
        savedLTA.setCalculatedCost(new BigDecimal("100.0"));

        when(cityRepository.findById(1L)).thenReturn(Optional.of(originCity));
        when(cityRepository.findById(2L)).thenReturn(Optional.of(destinationCity));
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(ltaRepository.save(any(LTA.class))).thenReturn(savedLTA);
        when(ltaRepository.existsByLtaNumber(anyString())).thenReturn(false);
        when(fiscalYearService.getCurrentFiscalYear()).thenReturn(fiscalYear);
        when(accountService.getAccountByNumber("411")).thenReturn(clientAccount);
        when(accountService.getAccountByNumber("701")).thenReturn(salesAccount);

        // When
        ltaService.createLTA(ltaRequest);

        // Then
        verify(journalEntryService).saveJournalEntry(argThat(journalEntry -> {
            return journalEntry.getSourceType() == SourceType.LTA &&
                    journalEntry.getSourceId().equals(1L) &&
                    journalEntry.getReference().equals("LTA-TEST-003") &&
                    journalEntry.getDescription().contains("Création LTA LTA-TEST-003");
        }));
    }

    @Test
    void testCreateLTA_WithoutClient_ShouldWork() {
        // Given - LTA request without client (CASH payment)
        ltaRequest.setClientId(null);
        ltaRequest.setPaymentMode(PaymentMode.CASH);

        LTA savedLTA = new LTA();
        savedLTA.setId(1L);
        savedLTA.setLtaNumber("LTA-TEST-004");
        savedLTA.setCalculatedCost(new BigDecimal("21.0"));

        when(cityRepository.findById(1L)).thenReturn(Optional.of(originCity));
        when(cityRepository.findById(2L)).thenReturn(Optional.of(destinationCity));
        when(ltaRepository.save(any(LTA.class))).thenReturn(savedLTA);
        when(ltaRepository.existsByLtaNumber(anyString())).thenReturn(false);
        when(fiscalYearService.getCurrentFiscalYear()).thenReturn(fiscalYear);
        when(accountService.getAccountByNumber("411")).thenReturn(clientAccount);
        when(accountService.getAccountByNumber("701")).thenReturn(salesAccount);

        // When
        LTA result = ltaService.createLTA(ltaRequest);

        // Then
        assertNotNull(result);
        verify(clientRepository, never()).findById(any());
        verify(journalEntryService).saveJournalEntry(any(JournalEntry.class));
    }
}
