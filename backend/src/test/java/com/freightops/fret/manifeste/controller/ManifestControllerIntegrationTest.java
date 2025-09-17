package com.freightops.fret.manifeste.controller;

import com.freightops.fret.manifeste.model.FreightManifest;
import com.freightops.fret.manifeste.model.ManifestItem;
import com.freightops.fret.manifeste.repository.FreightManifestRepository;
import com.freightops.fret.manifeste.repository.ManifestItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class ManifestControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FreightManifestRepository manifestRepository;

    @Autowired
    private ManifestItemRepository manifestItemRepository;

    private MockMvc mockMvc;
    private FreightManifest testManifest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Create test manifest
        testManifest = new FreightManifest();
        testManifest.setManifestNumber("MAN-2024-001");
        testManifest.setProformaNumber("PRO-2024-001");
        testManifest.setTransportMode("ROAD");
        testManifest.setVehicleReference("TRK-001");
        testManifest.setDriverName("Jean Dupont");
        testManifest.setScheduledDeparture(LocalDateTime.now().plusDays(1));
        testManifest.setScheduledArrival(LocalDateTime.now().plusDays(2));

        // Shipper
        testManifest.setShipperName("Expéditeur Test");
        testManifest.setShipperAddress("123 Rue de l'Expéditeur, Kinshasa");
        testManifest.setShipperPhone("+243 123 456 789");

        // Consignee
        testManifest.setConsigneeName("Destinataire Test");
        testManifest.setConsigneeAddress("456 Avenue du Destinataire, Lubumbashi");
        testManifest.setConsigneePhone("+243 987 654 321");

        // Totals
        testManifest.setTotalPackages(5);
        testManifest.setTotalWeight(new BigDecimal("150.50"));
        testManifest.setTotalVolume(new BigDecimal("2.75"));
        testManifest.setTotalValue(new BigDecimal("5000.00"));

        testManifest.setStatus("DRAFT");
        testManifest.setCreatedAt(LocalDateTime.now());
        testManifest.setUpdatedAt(LocalDateTime.now());

        testManifest = manifestRepository.save(testManifest);

        // Create test items
        List<ManifestItem> items = new ArrayList<>();

        ManifestItem item1 = new ManifestItem();
        item1.setManifest(testManifest);
        item1.setLineNumber(1);
        item1.setTrackingNumber("TRK-001-001");
        item1.setDescription("Ordinateurs portables");
        item1.setPackagingType("Cartons");
        item1.setPackageCount(2);
        item1.setGrossWeight(new BigDecimal("25.50"));
        item1.setVolume(new BigDecimal("0.75"));
        item1.setDeclaredValue(new BigDecimal("2500.00"));
        item1.setContainerNumber("CONT-001");
        items.add(item1);

        ManifestItem item2 = new ManifestItem();
        item2.setManifest(testManifest);
        item2.setLineNumber(2);
        item2.setTrackingNumber("TRK-001-002");
        item2.setDescription("Équipements électroniques");
        item2.setPackagingType("Caisses");
        item2.setPackageCount(3);
        item2.setGrossWeight(new BigDecimal("125.00"));
        item2.setVolume(new BigDecimal("2.00"));
        item2.setDeclaredValue(new BigDecimal("2500.00"));
        item2.setContainerNumber("CONT-002");
        items.add(item2);

        manifestItemRepository.saveAll(items);
    }

    @Test
    void testGenerateManifestPdf() throws Exception {
        mockMvc.perform(get("/api/fret/manifests/{id}/pdf", testManifest.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=manifest_" + testManifest.getManifestNumber() + ".pdf"))
                .andExpect(content().string(org.hamcrest.Matchers.notNullValue()));
    }

    @Test
    void testGenerateManifestWord() throws Exception {
        mockMvc.perform(get("/api/fret/manifests/{id}/word", testManifest.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/rtf"))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=manifest_" + testManifest.getManifestNumber() + ".rtf"))
                .andExpect(content().string(org.hamcrest.Matchers.notNullValue()));
    }

    @Test
    void testGenerateManifestPdf_NotFound() throws Exception {
        mockMvc.perform(get("/api/fret/manifests/{id}/pdf", 99999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGenerateManifestWord_NotFound() throws Exception {
        mockMvc.perform(get("/api/fret/manifests/{id}/word", 99999L))
                .andExpect(status().isNotFound());
    }
}
