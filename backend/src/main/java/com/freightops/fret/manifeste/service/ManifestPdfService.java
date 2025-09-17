package com.freightops.fret.manifeste.service;

import com.freightops.fret.manifeste.model.FreightManifest;
import com.freightops.fret.manifeste.model.ManifestItem;
import com.freightops.fret.manifeste.repository.FreightManifestRepository;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import net.sf.jasperreports.export.SimpleRtfExporterConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class ManifestPdfService {

    private static final Logger LOGGER = Logger.getLogger(ManifestPdfService.class.getName());

    @Autowired
    private FreightManifestRepository freightManifestRepository;

    /**
     * Generate manifest PDF
     */
    public byte[] generateManifestPdf(Long manifestId) {
        try {
            return generateReport(manifestId, "PDF");
        } catch (Exception e) {
            LOGGER.severe("Error generating manifest PDF: " + e.getMessage());
            throw new RuntimeException("Failed to generate manifest PDF", e);
        }
    }

    /**
     * Generate manifest Word document (RTF format)
     */
    public byte[] generateManifestWord(Long manifestId) {
        try {
            return generateReport(manifestId, "RTF");
        } catch (Exception e) {
            LOGGER.severe("Error generating manifest Word document: " + e.getMessage());
            throw new RuntimeException("Failed to generate manifest Word document", e);
        }
    }

    /**
     * Generate report in specified format
     */
    private byte[] generateReport(Long manifestId, String format) throws Exception {
        LOGGER.info("Starting report generation for manifest ID: " + manifestId + ", format: " + format);

        // Get manifest data
        FreightManifest manifest = freightManifestRepository.findById(manifestId)
                .orElseThrow(() -> new RuntimeException("Manifest not found with id: " + manifestId));

        LOGGER.info("Found manifest: " + manifest.getManifestNumber());

        // Load and compile template
        ClassPathResource resource = new ClassPathResource("reports/freight_manifest_template.jrxml");

        if (!resource.exists()) {
            LOGGER.severe("Template file not found: reports/freight_manifest_template.jrxml");
            throw new RuntimeException("Template file not found");
        }

        LOGGER.info("Template file found, compiling...");
        JasperReport jasperReport;

        try (InputStream templateStream = resource.getInputStream()) {
            jasperReport = JasperCompileManager.compileReport(templateStream);
            LOGGER.info("Template compiled successfully");
        } catch (Exception e) {
            LOGGER.severe("Error compiling template: " + e.getMessage());
            throw new RuntimeException("Failed to compile template", e);
        }

        // Prepare parameters
        LOGGER.info("Preparing parameters...");
        Map<String, Object> parameters = prepareParameters(manifest);
        LOGGER.info("Parameters prepared: " + parameters.size() + " parameters");

        // Prepare data source for items
        LOGGER.info("Preparing items data...");
        List<ManifestItemData> itemsData = prepareItemsData(manifest);
        LOGGER.info("Items data prepared: " + itemsData.size() + " items");
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(itemsData);

        // Fill report
        LOGGER.info("Filling report...");
        JasperPrint jasperPrint;
        try {
            jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
            LOGGER.info("Report filled successfully");
        } catch (Exception e) {
            LOGGER.severe("Error filling report: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to fill report", e);
        }

        // Export to desired format
        LOGGER.info("Exporting to " + format + "...");
        byte[] result;
        if ("RTF".equals(format)) {
            result = exportToRtf(jasperPrint);
        } else {
            result = exportToPdf(jasperPrint);
        }

        LOGGER.info("Report generated successfully, size: " + result.length + " bytes");
        return result;
    }

    /**
     * Export to PDF format
     */
    private byte[] exportToPdf(JasperPrint jasperPrint) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        JRPdfExporter exporter = new JRPdfExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

        SimplePdfExporterConfiguration configuration = new SimplePdfExporterConfiguration();
        exporter.setConfiguration(configuration);
        exporter.exportReport();

        return outputStream.toByteArray();
    }

    /**
     * Export to RTF format (Word compatible)
     */
    private byte[] exportToRtf(JasperPrint jasperPrint) throws Exception {
        StringWriter stringWriter = new StringWriter();

        JRRtfExporter exporter = new JRRtfExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleWriterExporterOutput(stringWriter));

        SimpleRtfExporterConfiguration configuration = new SimpleRtfExporterConfiguration();
        exporter.setConfiguration(configuration);
        exporter.exportReport();

        return stringWriter.toString().getBytes();
    }

    /**
     * Prepare parameters for JasperReports template
     */
    private Map<String, Object> prepareParameters(FreightManifest manifest) {
        Map<String, Object> parameters = new HashMap<>();

        // Basic manifest information
        parameters.put("manifestNumber", manifest.getManifestNumber());
        parameters.put("proformaNumber", manifest.getProformaNumber());
        parameters.put("transportMode", getTransportModeLabel(manifest.getTransportMode()));
        parameters.put("vehicleReference", manifest.getVehicleReference());
        parameters.put("driverName", manifest.getDriverName());
        parameters.put("driverPhone", manifest.getDriverPhone());
        parameters.put("scheduledDeparture",
                Date.from(manifest.getScheduledDeparture().atZone(ZoneId.systemDefault()).toInstant()));
        parameters.put("scheduledArrival",
                Date.from(manifest.getScheduledArrival().atZone(ZoneId.systemDefault()).toInstant()));
        parameters.put("status", getStatusLabel(manifest.getStatus()));
        parameters.put("qrCode", manifest.getQrCode());

        // Shipper information
        parameters.put("shipperName", manifest.getShipperName());
        parameters.put("shipperAddress", manifest.getShipperAddress());
        parameters.put("shipperContact", manifest.getShipperContact());
        parameters.put("shipperPhone", manifest.getShipperPhone());

        // Consignee information
        parameters.put("consigneeName", manifest.getConsigneeName());
        parameters.put("consigneeAddress", manifest.getConsigneeAddress());
        parameters.put("consigneeContact", manifest.getConsigneeContact());
        parameters.put("consigneePhone", manifest.getConsigneePhone());

        // Client information
        parameters.put("clientName", manifest.getClientName());
        parameters.put("clientReference", manifest.getClientReference());

        // Agent information
        parameters.put("agentName", manifest.getAgentName());
        parameters.put("agentAddress", manifest.getAgentAddress());

        // Totals
        parameters.put("totalWeight", manifest.getTotalWeight() != null ? manifest.getTotalWeight() : BigDecimal.ZERO);
        parameters.put("totalVolume", manifest.getTotalVolume() != null ? manifest.getTotalVolume() : BigDecimal.ZERO);
        parameters.put("totalPackages", manifest.getTotalPackages() != null ? manifest.getTotalPackages() : 0);
        parameters.put("totalValue", manifest.getTotalValue() != null ? manifest.getTotalValue() : BigDecimal.ZERO);

        // Instructions and remarks
        parameters.put("deliveryInstructions", manifest.getDeliveryInstructions());
        parameters.put("generalRemarks", manifest.getGeneralRemarks());

        return parameters;
    }

    /**
     * Prepare items data for JasperReports
     */
    private List<ManifestItemData> prepareItemsData(FreightManifest manifest) {
        List<ManifestItemData> itemsData = new ArrayList<>();

        if (manifest.getItems() != null && !manifest.getItems().isEmpty()) {
            for (ManifestItem item : manifest.getItems()) {
                ManifestItemData itemData = new ManifestItemData();
                itemData.setLineNumber(item.getLineNumber());
                itemData.setTrackingNumber(item.getTrackingNumber());
                itemData.setDescription(item.getDescription());
                itemData.setPackagingType(item.getPackagingType());
                itemData.setPackageCount(item.getPackageCount());
                itemData.setGrossWeight(item.getGrossWeight() != null ? item.getGrossWeight() : BigDecimal.ZERO);
                itemData.setVolume(item.getVolume() != null ? item.getVolume() : BigDecimal.ZERO);
                itemData.setVolumetricWeight(
                        item.getVolumetricWeight() != null ? item.getVolumetricWeight() : BigDecimal.ZERO);
                itemData.setDeclaredValue(item.getDeclaredValue() != null ? item.getDeclaredValue() : BigDecimal.ZERO);
                itemData.setContainerNumber(item.getContainerNumber());
                itemData.setRemarks(item.getRemarks());
                itemsData.add(itemData);
            }
        } else {
            // Add empty row if no items to prevent template errors
            ManifestItemData emptyItem = new ManifestItemData();
            emptyItem.setLineNumber(1);
            emptyItem.setTrackingNumber("");
            emptyItem.setDescription("Aucun article");
            emptyItem.setPackagingType("");
            emptyItem.setPackageCount(0);
            emptyItem.setGrossWeight(BigDecimal.ZERO);
            emptyItem.setVolume(BigDecimal.ZERO);
            emptyItem.setVolumetricWeight(BigDecimal.ZERO);
            emptyItem.setDeclaredValue(BigDecimal.ZERO);
            emptyItem.setContainerNumber("");
            emptyItem.setRemarks("");
            itemsData.add(emptyItem);
        }

        return itemsData;
    }

    /**
     * Get transport mode label in French
     */
    private String getTransportModeLabel(String transportMode) {
        if (transportMode == null)
            return "Non spécifié";

        switch (transportMode.toUpperCase()) {
            case "AIR":
                return "Aérien";
            case "ROAD":
                return "Routier";
            case "SEA":
                return "Maritime";
            case "RAIL":
                return "Ferroviaire";
            case "MULTIMODAL":
                return "Multimodal";
            default:
                return transportMode;
        }
    }

    /**
     * Get status label in French
     */
    private String getStatusLabel(String status) {
        if (status == null)
            return "Non défini";

        switch (status.toUpperCase()) {
            case "DRAFT":
                return "Brouillon";
            case "CONFIRMED":
                return "Confirmé";
            case "IN_TRANSIT":
                return "En Transit";
            case "DELIVERED":
                return "Livré";
            case "CANCELLED":
                return "Annulé";
            default:
                return status;
        }
    }

    /**
     * Data class for manifest items in JasperReports
     */
    public static class ManifestItemData {
        private Integer lineNumber;
        private String trackingNumber;
        private String description;
        private String packagingType;
        private Integer packageCount;
        private BigDecimal grossWeight;
        private BigDecimal volume;
        private BigDecimal volumetricWeight;
        private BigDecimal declaredValue;
        private String containerNumber;
        private String remarks;

        // Getters and setters
        public Integer getLineNumber() {
            return lineNumber;
        }

        public void setLineNumber(Integer lineNumber) {
            this.lineNumber = lineNumber;
        }

        public String getTrackingNumber() {
            return trackingNumber;
        }

        public void setTrackingNumber(String trackingNumber) {
            this.trackingNumber = trackingNumber;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getPackagingType() {
            return packagingType;
        }

        public void setPackagingType(String packagingType) {
            this.packagingType = packagingType;
        }

        public Integer getPackageCount() {
            return packageCount;
        }

        public void setPackageCount(Integer packageCount) {
            this.packageCount = packageCount;
        }

        public BigDecimal getGrossWeight() {
            return grossWeight;
        }

        public void setGrossWeight(BigDecimal grossWeight) {
            this.grossWeight = grossWeight;
        }

        public BigDecimal getVolume() {
            return volume;
        }

        public void setVolume(BigDecimal volume) {
            this.volume = volume;
        }

        public BigDecimal getVolumetricWeight() {
            return volumetricWeight;
        }

        public void setVolumetricWeight(BigDecimal volumetricWeight) {
            this.volumetricWeight = volumetricWeight;
        }

        public BigDecimal getDeclaredValue() {
            return declaredValue;
        }

        public void setDeclaredValue(BigDecimal declaredValue) {
            this.declaredValue = declaredValue;
        }

        public String getContainerNumber() {
            return containerNumber;
        }

        public void setContainerNumber(String containerNumber) {
            this.containerNumber = containerNumber;
        }

        public String getRemarks() {
            return remarks;
        }

        public void setRemarks(String remarks) {
            this.remarks = remarks;
        }
    }
}
