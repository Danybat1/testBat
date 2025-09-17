package com.freightops.fret.manifeste.service;

import com.freightops.fret.manifeste.model.FreightManifest;
import com.freightops.fret.manifeste.model.ManifestItem;
import com.freightops.fret.manifeste.repository.FreightManifestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManifestPdfServiceTest {

    @Mock
    private FreightManifestRepository freightManifestRepository;

    @InjectMocks
    private ManifestPdfService manifestPdfService;

    private FreightManifest testManifest;

    @BeforeEach
    void setUp() {
        testManifest = createTestManifest();
    }

    @Test
    void testGenerateManifestPdf() {
        // Given
        Long manifestId = 1L;
        when(freightManifestRepository.findById(manifestId)).thenReturn(Optional.of(testManifest));

        // When
        byte[] pdfBytes = manifestPdfService.generateManifestPdf(manifestId);

        // Then
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        verify(freightManifestRepository).findById(manifestId);
    }

    @Test
    void testGenerateManifestWord() {
        // Given
        Long manifestId = 1L;
        when(freightManifestRepository.findById(manifestId)).thenReturn(Optional.of(testManifest));

        // When
        byte[] wordBytes = manifestPdfService.generateManifestWord(manifestId);

        // Then
        assertNotNull(wordBytes);
        assertTrue(wordBytes.length > 0);
        verify(freightManifestRepository).findById(manifestId);
    }

    @Test
    void testGenerateManifestPdf_ManifestNotFound() {
        // Given
        Long manifestId = 999L;
        when(freightManifestRepository.findById(manifestId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            manifestPdfService.generateManifestPdf(manifestId);
        });

        assertTrue(exception.getMessage().contains("Manifest not found"));
        verify(freightManifestRepository).findById(manifestId);
    }

    private FreightManifest createTestManifest() {
        FreightManifest manifest = new FreightManifest();

        // Basic information
        manifest.setId(1L);
        manifest.setManifestNumber("MAN-2024-001");
        manifest.setProformaNumber("PRO-2024-001");
        manifest.setTransportMode("ROAD");
        manifest.setVehicleReference("CAM-123-AB");
        manifest.setDriverName("Jean Dupont");
        manifest.setDriverPhone("+237 6XX XXX XXX");
        manifest.setScheduledDeparture(LocalDateTime.now().plusDays(1));
        manifest.setScheduledArrival(LocalDateTime.now().plusDays(2));
        manifest.setStatus("CONFIRMED");
        manifest.setQrCode("https://freightops.com/track/MAN-2024-001");

        // Shipper information
        manifest.setShipperName("Société Export SARL");
        manifest.setShipperAddress("123 Rue du Commerce, Douala, Cameroun");
        manifest.setShipperContact("Marie Ngono");
        manifest.setShipperPhone("+237 6XX XXX XXX");

        // Consignee information
        manifest.setConsigneeName("Import & Co Ltd");
        manifest.setConsigneeAddress("456 Avenue de l'Indépendance, Yaoundé, Cameroun");
        manifest.setConsigneeContact("Paul Mbarga");
        manifest.setConsigneePhone("+237 6YY YYY YYY");

        // Client information
        manifest.setClientName("FreightOps Client");
        manifest.setClientReference("CLI-2024-001");

        // Agent information
        manifest.setAgentName("Agent FreightOps");
        manifest.setAgentAddress("FreightOps Douala, Cameroun");

        // Totals
        manifest.setTotalWeight(new BigDecimal("150.50"));
        manifest.setTotalVolume(new BigDecimal("2.500"));
        manifest.setTotalPackages(5);
        manifest.setTotalValue(new BigDecimal("250000"));

        // Instructions
        manifest.setDeliveryInstructions("Livraison en matinée uniquement. Fragile - Manipuler avec précaution.");
        manifest.setGeneralRemarks("Marchandises périssables - Transport réfrigéré requis");

        // Create test items
        List<ManifestItem> items = createTestItems(manifest);
        manifest.setItems(items);

        return manifest;
    }

    private List<ManifestItem> createTestItems(FreightManifest manifest) {
        List<ManifestItem> items = new ArrayList<>();

        // Item 1
        ManifestItem item1 = new ManifestItem();
        item1.setId(1L);
        item1.setManifest(manifest);
        item1.setLineNumber(1);
        item1.setTrackingNumber("TRK-001-2024");
        item1.setDescription("Équipements électroniques - Ordinateurs portables");
        item1.setPackagingType("Cartons");
        item1.setPackageCount(3);
        item1.setGrossWeight(new BigDecimal("45.50"));
        item1.setVolume(new BigDecimal("0.750"));
        item1.setVolumetricWeight(new BigDecimal("125.00"));
        item1.setDeclaredValue(new BigDecimal("150000"));
        item1.setContainerNumber("CONT-001");
        item1.setRemarks("Fragile - Ne pas retourner");
        items.add(item1);

        // Item 2
        ManifestItem item2 = new ManifestItem();
        item2.setId(2L);
        item2.setManifest(manifest);
        item2.setLineNumber(2);
        item2.setTrackingNumber("TRK-002-2024");
        item2.setDescription("Pièces automobiles - Moteurs");
        item2.setPackagingType("Palettes");
        item2.setPackageCount(2);
        item2.setGrossWeight(new BigDecimal("105.00"));
        item2.setVolume(new BigDecimal("1.750"));
        item2.setVolumetricWeight(new BigDecimal("291.67"));
        item2.setDeclaredValue(new BigDecimal("100000"));
        item2.setContainerNumber("CONT-002");
        item2.setRemarks("Pièces lourdes - Utiliser chariot élévateur");
        items.add(item2);

        return items;
    }
}
