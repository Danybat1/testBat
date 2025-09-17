package com.freightops.service;

import com.freightops.entity.Invoice;
import com.freightops.entity.InvoiceItem;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Service
public class InvoiceJasperService {

    private static final String INVOICE_TEMPLATE_PATH = "reports/invoice_modern_template.jrxml";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generateInvoicePdf(Invoice invoice) throws Exception {
        try {
            System.out.println("=== DEBUT GENERATION PDF ===");
            System.out.println("Invoice ID: " + (invoice != null ? invoice.getId() : "null"));

            // Validate input
            if (invoice == null) {
                System.err.println("ERREUR: Invoice est null");
                throw new IllegalArgumentException("Invoice cannot be null");
            }

            System.out.println("Invoice Number: " + invoice.getInvoiceNumber());
            System.out.println("Invoice Type: " + invoice.getType());
            System.out.println("Invoice Status: " + invoice.getStatus());
            System.out.println("Client: " + (invoice.getClient() != null ? invoice.getClient().getName() : "null"));

            if (invoice.getItems() == null || invoice.getItems().isEmpty()) {
                System.err.println("ERREUR: Invoice n'a pas d'items");
                throw new IllegalArgumentException("Invoice must have at least one item");
            }

            System.out.println("Nombre d'items: " + invoice.getItems().size());

            // Log each item
            for (int i = 0; i < invoice.getItems().size(); i++) {
                InvoiceItem item = invoice.getItems().get(i);
                System.out.println("Item " + i + ": " + item.getDescription() +
                        " - Qty: " + item.getQuantity() +
                        " - Price: " + item.getUnitPrice() +
                        " - Total: " + item.getTotalPrice());
            }

            System.out.println("Chargement du template: " + INVOICE_TEMPLATE_PATH);

            // Load the template
            ClassPathResource resource = new ClassPathResource(INVOICE_TEMPLATE_PATH);
            if (!resource.exists()) {
                System.err.println("ERREUR: Template non trouvé: " + INVOICE_TEMPLATE_PATH);
                throw new RuntimeException("Template non trouvé: " + INVOICE_TEMPLATE_PATH);
            }

            InputStream templateStream = resource.getInputStream();
            System.out.println("Template chargé avec succès");

            // Compile the report
            System.out.println("Compilation du rapport...");
            JasperReport jasperReport = JasperCompileManager.compileReport(templateStream);
            System.out.println("Rapport compilé avec succès");

            // Prepare parameters with null checks
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("invoiceNumber", invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber() : "N/A");
            parameters.put("invoiceDate",
                    invoice.getInvoiceDate() != null ? invoice.getInvoiceDate().format(DATE_FORMATTER) : "N/A");
            parameters.put("dueDate",
                    invoice.getDueDate() != null ? invoice.getDueDate().format(DATE_FORMATTER) : "N/A");
            parameters.put("status", getStatusLabel(invoice.getStatus()));
            parameters.put("type", getTypeLabel(invoice.getType()));
            parameters.put("subtotal",
                    invoice.getAmountExcludingTax() != null ? invoice.getAmountExcludingTax() : BigDecimal.ZERO);
            parameters.put("taxAmount", invoice.getTaxAmount() != null ? invoice.getTaxAmount() : BigDecimal.ZERO);
            parameters.put("totalAmount",
                    invoice.getTotalAmount() != null ? invoice.getTotalAmount() : BigDecimal.ZERO);
            parameters.put("currency", "CDF"); // Currency for DRC
            parameters.put("notes", invoice.getDescription() != null ? invoice.getDescription() : ""); // Use
                                                                                                       // description as
                                                                                                       // notes

            // Client information with null checks
            if (invoice.getClient() != null) {
                parameters.put("clientName",
                        invoice.getClient().getName() != null ? invoice.getClient().getName() : "Client non spécifié");
                parameters.put("clientAddress",
                        invoice.getClient().getAddress() != null ? invoice.getClient().getAddress() : "");
                parameters.put("clientPhone",
                        invoice.getClient().getContactNumber() != null ? invoice.getClient().getContactNumber() : "");
                parameters.put("clientEmail",
                        invoice.getClient().getEmail() != null ? invoice.getClient().getEmail() : "");
            } else {
                parameters.put("clientName", "Client non spécifié");
                parameters.put("clientAddress", "");
                parameters.put("clientPhone", "");
                parameters.put("clientEmail", "");
            }

            System.out.println("Paramètres préparés: " + parameters.keySet());

            // Prepare data source for invoice items
            System.out.println("Conversion des items...");
            List<InvoiceItemData> itemDataList = invoice.getItems().stream()
                    .map(this::convertToItemData)
                    .toList();
            System.out.println("Items convertis: " + itemDataList.size());

            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(itemDataList);
            System.out.println("DataSource créé");

            // Fill the report
            System.out.println("Remplissage du rapport...");
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
            System.out.println("Rapport rempli avec succès");

            // Export to PDF
            System.out.println("Export vers PDF...");
            byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);
            System.out.println("PDF généré avec succès. Taille: " + pdfBytes.length + " bytes");
            System.out.println("=== FIN GENERATION PDF ===");

            return pdfBytes;
        } catch (Exception e) {
            System.err.println("=== ERREUR GENERATION PDF ===");
            System.err.println("Erreur lors de la génération du PDF: " + e.getMessage());
            System.err.println("Type d'erreur: " + e.getClass().getSimpleName());
            e.printStackTrace();
            System.err.println("=== FIN ERREUR ===");
            throw new RuntimeException("Impossible de générer le PDF de la facture: " + e.getMessage(), e);
        }
    }

    /**
     * Generate HTML preview for invoice without creating PDF
     */
    public String generateInvoicePreviewHtml(Invoice invoice) throws Exception {
        try {
            System.out.println("=== DEBUT GENERATION HTML PREVIEW ===");
            System.out.println("Invoice ID: " + (invoice != null ? invoice.getId() : "null"));

            // Validate input
            if (invoice == null) {
                throw new IllegalArgumentException("Invoice cannot be null");
            }

            if (invoice.getItems() == null || invoice.getItems().isEmpty()) {
                throw new IllegalArgumentException("Invoice must have at least one item");
            }

            System.out.println("Nombre d'items: " + invoice.getItems().size());

            // Load and compile the template
            ClassPathResource resource = new ClassPathResource(INVOICE_TEMPLATE_PATH);
            InputStream templateStream = resource.getInputStream();
            JasperReport jasperReport = JasperCompileManager.compileReport(templateStream);
            System.out.println("Template compilé pour HTML");

            // Prepare parameters with null checks
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("invoiceNumber", invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber() : "N/A");
            parameters.put("invoiceDate",
                    invoice.getInvoiceDate() != null ? invoice.getInvoiceDate().format(DATE_FORMATTER) : "N/A");
            parameters.put("dueDate",
                    invoice.getDueDate() != null ? invoice.getDueDate().format(DATE_FORMATTER) : "N/A");
            parameters.put("type", getTypeLabel(invoice.getType()));
            parameters.put("status", getStatusLabel(invoice.getStatus()));
            parameters.put("subtotal",
                    invoice.getAmountExcludingTax() != null ? invoice.getAmountExcludingTax() : BigDecimal.ZERO);
            parameters.put("taxAmount", invoice.getTaxAmount() != null ? invoice.getTaxAmount() : BigDecimal.ZERO);
            parameters.put("totalAmount",
                    invoice.getTotalAmount() != null ? invoice.getTotalAmount() : BigDecimal.ZERO);
            parameters.put("currency", "CDF");
            parameters.put("notes", invoice.getDescription() != null ? invoice.getDescription() : "");

            // Client information with null checks
            if (invoice.getClient() != null) {
                parameters.put("clientName",
                        invoice.getClient().getName() != null ? invoice.getClient().getName() : "Client non spécifié");
                parameters.put("clientAddress",
                        invoice.getClient().getAddress() != null ? invoice.getClient().getAddress() : "");
                parameters.put("clientPhone",
                        invoice.getClient().getContactNumber() != null ? invoice.getClient().getContactNumber() : "");
                parameters.put("clientEmail",
                        invoice.getClient().getEmail() != null ? invoice.getClient().getEmail() : "");
            } else {
                parameters.put("clientName", "Client non spécifié");
                parameters.put("clientAddress", "");
                parameters.put("clientPhone", "");
                parameters.put("clientEmail", "");
            }

            // Convert items to data source
            List<InvoiceItemData> itemsData = invoice.getItems().stream()
                    .map(this::convertToItemData)
                    .collect(toList());
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(itemsData);
            System.out.println("DataSource créé pour HTML avec " + itemsData.size() + " items");

            // Fill the report
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
            System.out.println("Rapport rempli pour HTML");

            // Export to HTML using StringWriter to capture output
            java.io.StringWriter stringWriter = new java.io.StringWriter();
            HtmlExporter exporter = new HtmlExporter();
            SimpleHtmlExporterOutput output = new SimpleHtmlExporterOutput(stringWriter);
            SimpleExporterInput input = new SimpleExporterInput(jasperPrint);
            exporter.setExporterInput(input);
            exporter.setExporterOutput(output);
            exporter.exportReport();

            String htmlContent = stringWriter.toString();
            System.out.println("HTML généré avec succès. Taille: " + htmlContent.length() + " caractères");
            System.out.println("=== FIN GENERATION HTML PREVIEW ===");

            return htmlContent;
        } catch (Exception e) {
            System.err.println("=== ERREUR GENERATION HTML ===");
            System.err.println("Erreur lors de la génération de l'aperçu HTML: " + e.getMessage());
            e.printStackTrace();
            System.err.println("=== FIN ERREUR HTML ===");
            throw new Exception("Erreur lors de la génération de l'aperçu HTML: " + e.getMessage(), e);
        }
    }

    private InvoiceItemData convertToItemData(InvoiceItem item) {
        InvoiceItemData data = new InvoiceItemData();
        data.setDescription(item.getDescription());
        data.setQuantity(new BigDecimal(item.getQuantity()));
        data.setUnitPrice(item.getUnitPrice());
        data.setTotalPrice(item.getTotalPrice());

        // Use taxAmount directly instead of accessing Tax entity
        // This avoids lazy loading issues and null pointer exceptions
        data.setItemTaxAmount(item.getTaxAmount() != null ? item.getTaxAmount() : BigDecimal.ZERO);

        // Calculate tax rate from taxAmount and totalPrice if available
        if (item.getTotalPrice() != null && item.getTotalPrice().compareTo(BigDecimal.ZERO) > 0
                && item.getTaxAmount() != null && item.getTaxAmount().compareTo(BigDecimal.ZERO) > 0) {
            // Calculate tax rate as percentage: (taxAmount / totalPrice) * 100
            BigDecimal taxRate = item.getTaxAmount()
                    .divide(item.getTotalPrice(), 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            data.setTaxRate(taxRate);
        } else {
            data.setTaxRate(BigDecimal.ZERO);
        }

        return data;
    }

    private String getStatusLabel(com.freightops.enums.InvoiceStatus status) {
        switch (status) {
            case DRAFT:
                return "Brouillon";
            case SENT:
                return "Envoyée";
            case PAID:
                return "Payée";
            case PARTIALLY_PAID:
                return "Partiellement payée";
            case OVERDUE:
                return "En retard";
            case CANCELLED:
                return "Annulée";
            default:
                return status.toString();
        }
    }

    private String getTypeLabel(com.freightops.enums.InvoiceType type) {
        switch (type) {
            case CLIENT:
                return "Facture Client";
            case SUPPLIER:
                return "Facture Fournisseur";
            case TRANSPORT:
                return "Facture Transport";
            case PASSENGER:
                return "Facture Passager";
            case ADDITIONAL_SERVICES:
                return "Services Additionnels";
            case MIXED:
                return "Facture Mixte";
            default:
                return type.toString();
        }
    }

    // Data class for JasperReports
    public static class InvoiceItemData {
        private String description;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private BigDecimal taxRate;
        private BigDecimal itemTaxAmount;

        // Getters and setters
        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public BigDecimal getQuantity() {
            return quantity;
        }

        public void setQuantity(BigDecimal quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }

        public BigDecimal getTotalPrice() {
            return totalPrice;
        }

        public void setTotalPrice(BigDecimal totalPrice) {
            this.totalPrice = totalPrice;
        }

        public BigDecimal getTaxRate() {
            return taxRate;
        }

        public void setTaxRate(BigDecimal taxRate) {
            this.taxRate = taxRate;
        }

        public BigDecimal getItemTaxAmount() {
            return itemTaxAmount;
        }

        public void setItemTaxAmount(BigDecimal itemTaxAmount) {
            this.itemTaxAmount = itemTaxAmount;
        }
    }
}
