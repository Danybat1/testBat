package com.freightops.fret.manifeste.service;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test simple pour vérifier que JasperReports fonctionne correctement
 */
class SimpleJasperTest {

    @Test
    void testJasperReportsIsWorking() throws Exception {
        // Test que JasperReports peut compiler un template simple
        String simpleTemplate = """
                <?xml version="1.0" encoding="UTF-8"?>
                <jasperReport xmlns="http://jasperreports.sourceforge.net/jasperreports"
                             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                             xsi:schemaLocation="http://jasperreports.sourceforge.net/jasperreports
                             http://jasperreports.sourceforge.net/xsd/jasperreport.xsd"
                             name="simple_test" pageWidth="595" pageHeight="842">
                    <title>
                        <band height="50">
                            <staticText>
                                <reportElement x="0" y="0" width="200" height="30"/>
                                <text><![CDATA[Test JasperReports]]></text>
                            </staticText>
                        </band>
                    </title>
                </jasperReport>
                """;

        // Compile le template
        JasperReport report = JasperCompileManager.compileReport(
                new java.io.ByteArrayInputStream(simpleTemplate.getBytes()));

        assertNotNull(report, "Le rapport compilé ne doit pas être null");
        assertEquals("simple_test", report.getName(), "Le nom du rapport doit correspondre");
        assertEquals(595, report.getPageWidth(), "La largeur de page doit être 595");
        assertEquals(842, report.getPageHeight(), "La hauteur de page doit être 842");

        System.out.println("✓ JasperReports fonctionne correctement");
    }

    @Test
    void testManifestTemplateExists() throws Exception {
        // Vérifier que notre template de manifeste existe
        ClassPathResource resource = new ClassPathResource("reports/freight_manifest_template.jrxml");

        assertTrue(resource.exists(), "Le template freight_manifest_template.jrxml doit exister");

        try (InputStream stream = resource.getInputStream()) {
            assertTrue(stream.available() > 0, "Le template doit avoir du contenu");
        }

        System.out.println("✓ Template de manifeste trouvé");
    }

    @Test
    void testManifestTemplateCanBeCompiled() throws Exception {
        // Vérifier que notre template peut être compilé
        ClassPathResource resource = new ClassPathResource("reports/freight_manifest_template.jrxml");

        try (InputStream stream = resource.getInputStream()) {
            JasperReport report = JasperCompileManager.compileReport(stream);

            assertNotNull(report, "Le template compilé ne doit pas être null");
            assertEquals("freight_manifest_template", report.getName(), "Le nom doit correspondre");
            assertEquals(595, report.getPageWidth(), "Format A4 - largeur");
            assertEquals(842, report.getPageHeight(), "Format A4 - hauteur");

            System.out.println("✓ Template de manifeste compilé avec succès");
        }
    }
}
