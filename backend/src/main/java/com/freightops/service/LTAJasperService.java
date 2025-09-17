package com.freightops.service;

import com.freightops.entity.LTA;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LTAJasperService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generateLTAPdf(LTA lta) throws Exception {
        try {
            // Load the JRXML template
            ClassPathResource resource = new ClassPathResource("reports/lta_clean_template.jrxml");
            InputStream reportStream = resource.getInputStream();
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
            // Prepare parameters
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("shipperName", lta.getShipperName() != null ? lta.getShipperName() : "");
            parameters.put("shipperAddress", lta.getShipperAddress() != null ? lta.getShipperAddress() : "");
            parameters.put("consigneeName", lta.getConsigneeName() != null ? lta.getConsigneeName() : "");
            parameters.put("consigneeAddress", lta.getConsigneeAddress() != null ? lta.getConsigneeAddress() : "");
            parameters.put("clientName", lta.getClient() != null ? lta.getClient().getName() : "");
            parameters.put("paymentMode", getPaymentModeText(lta));
            parameters.put("originCity", lta.getOriginCity() != null ? lta.getOriginCity().getName() : "");
            parameters.put("originIata", lta.getOriginCity() != null ? lta.getOriginCity().getIataCode() : "");
            parameters.put("destinationCity",
                    lta.getDestinationCity() != null ? lta.getDestinationCity().getName() : "");
            parameters.put("destinationIata",
                    lta.getDestinationCity() != null ? lta.getDestinationCity().getIataCode() : "");
            parameters.put("ltaNumber", lta.getLtaNumber() != null ? lta.getLtaNumber() : "");
            parameters.put("packageCount", lta.getPackageCount() != null ? lta.getPackageCount() : 0);
            parameters.put("totalWeight", lta.getTotalWeight() != null ? lta.getTotalWeight() : BigDecimal.ZERO);
            parameters.put("calculatedCost",
                    lta.getCalculatedCost() != null ? lta.getCalculatedCost() : BigDecimal.ZERO);
            parameters.put("createdAt", lta.getCreatedAt() != null ? lta.getCreatedAt().format(DATE_FORMATTER) : "");
            parameters.put("packageNature", lta.getPackageNature() != null ? lta.getPackageNature() : "");

            // Create data source with one empty record to trigger detail band
            List<Object> dataList = new ArrayList<>();
            dataList.add(new Object()); // Add one empty object to trigger detail band
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(dataList);

            // Fill the report
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            // Export to PDF
            byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);

            return pdfBytes;

        } catch (Exception e) {
            System.err.println("JasperReports Error: " + e.getMessage());
            throw e;
        }
    }

    private String getPaymentModeText(LTA lta) {
        switch (lta.getPaymentMode()) {
            case CASH:
                return "CASH (Comptant)";
            case TO_INVOICE:
                return "TO_INVOICE (À facturer)";
            case FREIGHT_COLLECT:
                return "FREIGHT_COLLECT (Port dû)";
            case FREE:
                return "FREE (Gratuit)";
            default:
                return lta.getPaymentMode().toString();
        }
    }
}
