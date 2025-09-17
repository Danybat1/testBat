package com.freightops.fret.manifeste.service;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class ManifestTemplateTest {

    @Test
    void testTemplateCanBeLoaded() throws Exception {
        // Given
        ClassPathResource resource = new ClassPathResource("reports/freight_manifest_template.jrxml");

        // When & Then
        assertTrue(resource.exists(), "Template file should exist");

        try (InputStream templateStream = resource.getInputStream()) {
            assertNotNull(templateStream, "Template stream should not be null");
            assertTrue(templateStream.available() > 0, "Template should have content");
        }
    }

    @Test
    void testTemplateCanBeCompiled() throws Exception {
        // Given
        ClassPathResource resource = new ClassPathResource("reports/freight_manifest_template.jrxml");

        // When
        try (InputStream templateStream = resource.getInputStream()) {
            JasperReport jasperReport = JasperCompileManager.compileReport(templateStream);

            // Then
            assertNotNull(jasperReport, "Compiled report should not be null");
            assertEquals("freight_manifest_template", jasperReport.getName(), "Report name should match");
            assertEquals(595, jasperReport.getPageWidth(), "Page width should be A4 (595)");
            assertEquals(842, jasperReport.getPageHeight(), "Page height should be A4 (842)");
        }
    }

    @Test
    void testTemplateHasRequiredParameters() throws Exception {
        // Given
        ClassPathResource resource = new ClassPathResource("reports/freight_manifest_template.jrxml");

        // When
        try (InputStream templateStream = resource.getInputStream()) {
            JasperReport jasperReport = JasperCompileManager.compileReport(templateStream);

            // Then
            assertNotNull(jasperReport.getParameters(), "Parameters should not be null");
            assertTrue(jasperReport.getParameters().length > 0, "Should have parameters");

            // Check for some key parameters
            boolean hasManifestNumber = false;
            boolean hasShipperName = false;
            boolean hasConsigneeName = false;

            for (var param : jasperReport.getParameters()) {
                String paramName = param.getName();
                if ("manifestNumber".equals(paramName))
                    hasManifestNumber = true;
                if ("shipperName".equals(paramName))
                    hasShipperName = true;
                if ("consigneeName".equals(paramName))
                    hasConsigneeName = true;
            }

            assertTrue(hasManifestNumber, "Should have manifestNumber parameter");
            assertTrue(hasShipperName, "Should have shipperName parameter");
            assertTrue(hasConsigneeName, "Should have consigneeName parameter");
        }
    }

    @Test
    void testTemplateHasRequiredFields() throws Exception {
        // Given
        ClassPathResource resource = new ClassPathResource("reports/freight_manifest_template.jrxml");

        // When
        try (InputStream templateStream = resource.getInputStream()) {
            JasperReport jasperReport = JasperCompileManager.compileReport(templateStream);

            // Then
            assertNotNull(jasperReport.getFields(), "Fields should not be null");
            assertTrue(jasperReport.getFields().length > 0, "Should have fields for items");

            // Check for some key fields
            boolean hasLineNumber = false;
            boolean hasTrackingNumber = false;
            boolean hasDescription = false;

            for (var field : jasperReport.getFields()) {
                String fieldName = field.getName();
                if ("lineNumber".equals(fieldName))
                    hasLineNumber = true;
                if ("trackingNumber".equals(fieldName))
                    hasTrackingNumber = true;
                if ("description".equals(fieldName))
                    hasDescription = true;
            }

            assertTrue(hasLineNumber, "Should have lineNumber field");
            assertTrue(hasTrackingNumber, "Should have trackingNumber field");
            assertTrue(hasDescription, "Should have description field");
        }
    }
}
