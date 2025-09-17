package com.freightops.controller;

import com.freightops.entity.Invoice;
import com.freightops.entity.InvoiceItem;
import com.freightops.entity.Client;
import com.freightops.service.InvoiceService;
import com.freightops.service.InvoiceJasperService;
import com.freightops.service.ClientService;
import com.freightops.dto.InvoiceCreateRequest;
import com.freightops.dto.InvoiceItemDto;
import com.freightops.enums.InvoiceStatus;
import com.freightops.enums.InvoiceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/invoices")
@CrossOrigin(origins = "*")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private InvoiceJasperService invoiceJasperService;

    @Autowired
    private ClientService clientService;

    @GetMapping
    public ResponseEntity<List<Invoice>> getAllInvoices() {
        List<Invoice> invoices = invoiceService.getAllInvoices();
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<Invoice>> getInvoicesByClient(@PathVariable Long clientId) {
        List<Invoice> invoices = invoiceService.getInvoicesByClient(clientId);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Invoice>> getInvoicesByType(@PathVariable InvoiceType type) {
        List<Invoice> invoices = invoiceService.getInvoicesByType(type);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Invoice>> getInvoicesByStatus(@PathVariable InvoiceStatus status) {
        List<Invoice> invoices = invoiceService.getInvoicesByStatus(status);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<Invoice>> getOverdueInvoices() {
        List<Invoice> invoices = invoiceService.getOverdueInvoices();
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<Invoice>> getInvoicesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Invoice> invoices = invoiceService.getInvoicesByDateRange(startDate, endDate);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/type/{type}/date-range")
    public ResponseEntity<List<Invoice>> getInvoicesByTypeAndDateRange(
            @PathVariable InvoiceType type,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Invoice> invoices = invoiceService.getInvoicesByTypeAndDateRange(type, startDate, endDate);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Invoice> getInvoiceById(@PathVariable Long id) {
        return invoiceService.getInvoiceById(id)
                .map(invoice -> ResponseEntity.ok(invoice))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{invoiceNumber}")
    public ResponseEntity<Invoice> getInvoiceByNumber(@PathVariable String invoiceNumber) {
        return invoiceService.getInvoiceByNumber(invoiceNumber)
                .map(invoice -> ResponseEntity.ok(invoice))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stats/paid-amount")
    public ResponseEntity<BigDecimal> getTotalPaidAmount(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        BigDecimal total = invoiceService.getTotalPaidAmount(startDate, endDate);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/stats/remaining-amount")
    public ResponseEntity<BigDecimal> getTotalRemainingAmount() {
        BigDecimal total = invoiceService.getTotalRemainingAmount();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("InvoiceController is working!");
    }

    @PostMapping("/simple")
    public ResponseEntity<?> createInvoiceSimple(@RequestBody Map<String, Object> requestData) {
        try {
            return ResponseEntity.ok("Request received successfully: " + requestData.keySet());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createInvoice(@Valid @RequestBody InvoiceCreateRequest request) {
        try {
            // Validate required fields
            if (request.getClientId() == null) {
                return ResponseEntity.badRequest().body("Client ID est requis");
            }

            if (request.getItems() == null || request.getItems().isEmpty()) {
                return ResponseEntity.badRequest().body("Au moins un article est requis");
            }

            // Get client
            Optional<Client> clientOpt = clientService.getClientById(request.getClientId());
            if (clientOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Client non trouvé avec l'ID: " + request.getClientId());
            }
            Client client = clientOpt.get();

            // Generate invoice number
            String invoiceNumber = generateInvoiceNumber();

            // Create Invoice entity
            Invoice invoice = new Invoice();
            invoice.setInvoiceNumber(invoiceNumber);
            invoice.setType(request.getType());
            invoice.setClient(client);
            invoice.setInvoiceDate(request.getInvoiceDate());
            invoice.setDueDate(request.getDueDate());
            invoice.setDescription(request.getNotes());
            invoice.setStatus(InvoiceStatus.DRAFT);

            // Create and set invoice items
            List<InvoiceItem> items = request.getItems().stream().map(itemDto -> {
                InvoiceItem item = new InvoiceItem();
                item.setDescription(itemDto.getDescription());
                item.setQuantity(itemDto.getQuantity());
                item.setUnitPrice(itemDto.getUnitPrice());
                item.setInvoice(invoice);
                return item;
            }).collect(Collectors.toList());

            invoice.setItems(items);

            // Recalculate totals based on entity calculations
            BigDecimal recalculatedSubtotal = BigDecimal.ZERO;
            BigDecimal recalculatedTax = BigDecimal.ZERO;

            for (InvoiceItem item : items) {
                if (item.getTotalPrice() != null) {
                    recalculatedSubtotal = recalculatedSubtotal.add(item.getTotalPrice());
                }
                if (item.getTaxAmount() != null) {
                    recalculatedTax = recalculatedTax.add(item.getTaxAmount());
                }
            }

            invoice.setAmountExcludingTax(recalculatedSubtotal);
            invoice.setTaxAmount(recalculatedTax);
            invoice.setTotalAmount(recalculatedSubtotal.add(recalculatedTax));

            Invoice createdInvoice = invoiceService.createInvoice(invoice);

            return ResponseEntity.ok(createdInvoice);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la création de la facture: " + e.getMessage());
        }
    }

    private String generateInvoiceNumber() {
        String year = String.valueOf(LocalDate.now().getYear());
        long timestamp = System.currentTimeMillis();
        String sequence = String.format("%06d", timestamp % 1000000);
        return "INV-" + year + "-" + sequence;
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateInvoice(@PathVariable Long id, @Valid @RequestBody Invoice invoiceDetails) {
        try {
            Invoice updatedInvoice = invoiceService.updateInvoice(id, invoiceDetails);
            return ResponseEntity.ok(updatedInvoice);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteInvoice(@PathVariable Long id) {
        try {
            invoiceService.deleteInvoice(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<?> addItemToInvoice(@PathVariable Long id, @Valid @RequestBody InvoiceItem item) {
        try {
            Invoice updatedInvoice = invoiceService.addItemToInvoice(id, item);
            return ResponseEntity.ok(updatedInvoice);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{invoiceId}/items/{itemId}")
    public ResponseEntity<?> removeItemFromInvoice(@PathVariable Long invoiceId, @PathVariable Long itemId) {
        try {
            Invoice updatedInvoice = invoiceService.removeItemFromInvoice(invoiceId, itemId);
            return ResponseEntity.ok(updatedInvoice);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateInvoiceStatus(@PathVariable Long id, @RequestBody InvoiceStatus status) {
        try {
            Invoice updatedInvoice = invoiceService.updateInvoiceStatus(id, status);
            return ResponseEntity.ok(updatedInvoice);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/update-overdue")
    public ResponseEntity<?> updateOverdueInvoices() {
        try {
            invoiceService.updateOverdueInvoices();
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> generateInvoicePdf(@PathVariable Long id) {
        try {
            Optional<Invoice> invoiceOpt = invoiceService.getInvoiceById(id);
            if (invoiceOpt.isEmpty()) {
                System.err.println("Facture non trouvée avec l'ID: " + id);
                return ResponseEntity.notFound().build();
            }

            Invoice invoice = invoiceOpt.get();

            // Validate invoice data before PDF generation
            if (invoice.getItems() == null || invoice.getItems().isEmpty()) {
                System.err.println("Facture " + id + " n'a pas d'articles pour générer le PDF");
                return ResponseEntity.badRequest().build();
            }

            byte[] pdfBytes = invoiceJasperService.generateInvoicePdf(invoice);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "facture-" + invoice.getInvoiceNumber() + ".pdf");
            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (IllegalArgumentException e) {
            System.err.println("Erreur de validation pour la facture " + id + ": " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("Erreur lors de la génération du PDF pour la facture " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<String> previewInvoice(@PathVariable Long id) {
        try {
            Optional<Invoice> invoiceOpt = invoiceService.getInvoiceById(id);
            if (invoiceOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Invoice invoice = invoiceOpt.get();

            if (invoice.getItems() == null || invoice.getItems().isEmpty()) {
                return ResponseEntity.badRequest().body("La facture n'a pas d'articles");
            }

            String htmlPreview = generateHtmlPreview(invoice);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_HTML);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(htmlPreview);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Erreur lors de la génération de l'aperçu: " + e.getMessage());
        }
    }

    private String generateHtmlPreview(Invoice invoice) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html lang='fr'>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("<title>Aperçu Facture ").append(invoice.getInvoiceNumber()).append("</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }");
        html.append(
                ".invoice-container { max-width: 800px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }");
        html.append(
                ".header { background: #2563EB; color: white; padding: 20px; margin: -30px -30px 30px -30px; border-radius: 8px 8px 0 0; }");
        html.append(".header h1 { margin: 0; font-size: 24px; }");
        html.append(".invoice-info { display: flex; justify-content: space-between; margin-bottom: 30px; }");
        html.append(".client-info, .invoice-details { flex: 1; }");
        html.append(".client-info { margin-right: 20px; }");
        html.append(".info-section h3 { color: #374151; margin-bottom: 10px; font-size: 16px; }");
        html.append(".info-section p { margin: 5px 0; color: #6B7280; }");
        html.append(".items-table { width: 100%; border-collapse: collapse; margin: 20px 0; }");
        html.append(
                ".items-table th { background: #F3F4F6; padding: 12px; text-align: left; border-bottom: 2px solid #E5E7EB; color: #374151; font-weight: 600; }");
        html.append(".items-table td { padding: 12px; border-bottom: 1px solid #E5E7EB; }");
        html.append(".items-table tr:hover { background: #F9FAFB; }");
        html.append(".totals { margin-top: 30px; }");
        html.append(
                ".totals-box { background: #F1F5F9; padding: 20px; border-radius: 6px; border-left: 4px solid #2563EB; }");
        html.append(".total-row { display: flex; justify-content: space-between; margin: 8px 0; }");
        html.append(
                ".total-row.final { font-weight: bold; font-size: 18px; color: #1E40AF; border-top: 2px solid #2563EB; padding-top: 10px; margin-top: 15px; }");
        html.append(
                ".status-badge { display: inline-block; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; text-transform: uppercase; }");
        html.append(".status-draft { background: #FEF3C7; color: #92400E; }");
        html.append(".status-sent { background: #DBEAFE; color: #1E40AF; }");
        html.append(".status-paid { background: #D1FAE5; color: #065F46; }");
        html.append(".text-right { text-align: right; }");
        html.append(".notes { margin-top: 30px; padding: 15px; background: #F9FAFB; border-radius: 6px; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");

        html.append("<div class='invoice-container'>");

        // Header
        html.append("<div class='header'>");
        html.append("<h1>FreightOps - Facture</h1>");
        html.append("</div>");

        // Invoice info section
        html.append("<div class='invoice-info'>");

        // Client info
        html.append("<div class='client-info info-section'>");
        html.append("<h3>Informations Client</h3>");
        if (invoice.getClient() != null) {
            html.append("<p><strong>").append(invoice.getClient().getName()).append("</strong></p>");
            if (invoice.getClient().getAddress() != null) {
                html.append("<p>").append(invoice.getClient().getAddress()).append("</p>");
            }
            if (invoice.getClient().getContactNumber() != null) {
                html.append("<p>Tél: ").append(invoice.getClient().getContactNumber()).append("</p>");
            }
            if (invoice.getClient().getEmail() != null) {
                html.append("<p>Email: ").append(invoice.getClient().getEmail()).append("</p>");
            }
        }
        html.append("</div>");

        // Invoice details
        html.append("<div class='invoice-details info-section'>");
        html.append("<h3>Détails Facture</h3>");
        html.append("<p><strong>N°: ").append(invoice.getInvoiceNumber()).append("</strong></p>");
        html.append("<p>Date: ").append(invoice.getInvoiceDate() != null ? invoice.getInvoiceDate().toString() : "N/A")
                .append("</p>");
        html.append("<p>Échéance: ").append(invoice.getDueDate() != null ? invoice.getDueDate().toString() : "N/A")
                .append("</p>");
        html.append("<p>Type: ").append(getTypeLabel(invoice.getType())).append("</p>");
        html.append("<p>Statut: <span class='status-badge status-").append(invoice.getStatus().toString().toLowerCase())
                .append("'>").append(getStatusLabel(invoice.getStatus())).append("</span></p>");
        html.append("</div>");

        html.append("</div>");

        // Items table
        html.append("<table class='items-table'>");
        html.append("<thead>");
        html.append("<tr>");
        html.append("<th>Description</th>");
        html.append("<th class='text-right'>Quantité</th>");
        html.append("<th class='text-right'>Prix unitaire</th>");
        html.append("<th class='text-right'>Total</th>");
        html.append("</tr>");
        html.append("</thead>");
        html.append("<tbody>");

        for (InvoiceItem item : invoice.getItems()) {
            html.append("<tr>");
            html.append("<td>").append(item.getDescription() != null ? item.getDescription() : "").append("</td>");
            html.append("<td class='text-right'>").append(item.getQuantity()).append("</td>");
            html.append("<td class='text-right'>").append(String.format("%.2f", item.getUnitPrice()))
                    .append(" CDF</td>");
            html.append("<td class='text-right'>").append(String.format("%.2f", item.getTotalPrice()))
                    .append(" CDF</td>");
            html.append("</tr>");
        }

        html.append("</tbody>");
        html.append("</table>");

        // Totals
        html.append("<div class='totals'>");
        html.append("<div class='totals-box'>");
        html.append("<div class='total-row'>");
        html.append("<span>Sous-total:</span>");
        html.append("<span>").append(String.format("%.2f", invoice.getAmountExcludingTax())).append(" CDF</span>");
        html.append("</div>");
        html.append("<div class='total-row'>");
        html.append("<span>TVA:</span>");
        html.append("<span>").append(String.format("%.2f", invoice.getTaxAmount())).append(" CDF</span>");
        html.append("</div>");
        html.append("<div class='total-row final'>");
        html.append("<span>TOTAL:</span>");
        html.append("<span>").append(String.format("%.2f", invoice.getTotalAmount())).append(" CDF</span>");
        html.append("</div>");
        html.append("</div>");
        html.append("</div>");

        // Notes
        if (invoice.getDescription() != null && !invoice.getDescription().trim().isEmpty()) {
            html.append("<div class='notes'>");
            html.append("<h3>Notes:</h3>");
            html.append("<p>").append(invoice.getDescription()).append("</p>");
            html.append("</div>");
        }

        html.append("</div>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }

    private String getStatusLabel(InvoiceStatus status) {
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

    private String getTypeLabel(InvoiceType type) {
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

    @GetMapping("/{id}/debug")
    public ResponseEntity<?> debugInvoice(@PathVariable Long id) {
        try {
            Optional<Invoice> invoiceOpt = invoiceService.getInvoiceById(id);
            if (invoiceOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Facture non trouvée avec l'ID: " + id);
            }

            Invoice invoice = invoiceOpt.get();

            // Create debug info
            Map<String, Object> debugInfo = new HashMap<>();
            debugInfo.put("invoiceId", invoice.getId());
            debugInfo.put("invoiceNumber", invoice.getInvoiceNumber());
            debugInfo.put("status", invoice.getStatus());
            debugInfo.put("type", invoice.getType());
            debugInfo.put("clientId", invoice.getClient() != null ? invoice.getClient().getId() : null);
            debugInfo.put("clientName", invoice.getClient() != null ? invoice.getClient().getName() : null);
            debugInfo.put("itemsCount", invoice.getItems() != null ? invoice.getItems().size() : 0);
            debugInfo.put("totalAmount", invoice.getTotalAmount());
            debugInfo.put("amountExcludingTax", invoice.getAmountExcludingTax());
            debugInfo.put("taxAmount", invoice.getTaxAmount());

            if (invoice.getItems() != null && !invoice.getItems().isEmpty()) {
                List<Map<String, Object>> itemsDebug = invoice.getItems().stream().map(item -> {
                    Map<String, Object> itemInfo = new HashMap<>();
                    itemInfo.put("id", item.getId());
                    itemInfo.put("description", item.getDescription());
                    itemInfo.put("quantity", item.getQuantity());
                    itemInfo.put("unitPrice", item.getUnitPrice());
                    itemInfo.put("totalPrice", item.getTotalPrice());
                    itemInfo.put("taxAmount", item.getTaxAmount());
                    return itemInfo;
                }).collect(Collectors.toList());
                debugInfo.put("items", itemsDebug);
            }

            return ResponseEntity.ok(debugInfo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors du debug: " + e.getMessage());
        }
    }

    /**
     * Generate invoice preview without creating the invoice in database
     */
    @PostMapping("/preview")
    public ResponseEntity<String> generateInvoicePreview(@RequestBody InvoiceCreateRequest request) {
        try {
            // Validate client exists
            Optional<Client> clientOpt = clientService.getClientById(request.getClientId());
            if (clientOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Client introuvable avec l'ID: " + request.getClientId());
            }
            Client client = clientOpt.get();

            // Generate temporary invoice number for preview
            String previewInvoiceNumber = "PREVIEW-" + System.currentTimeMillis();

            // Create temporary Invoice entity (not persisted)
            Invoice tempInvoice = new Invoice();
            tempInvoice.setId(999999L); // Temporary ID for preview
            tempInvoice.setInvoiceNumber(previewInvoiceNumber);
            tempInvoice.setType(request.getType());
            tempInvoice.setClient(client);
            tempInvoice.setInvoiceDate(request.getInvoiceDate());
            tempInvoice.setDueDate(request.getDueDate());
            tempInvoice.setDescription(request.getNotes());
            tempInvoice.setStatus(InvoiceStatus.DRAFT);

            // Create temporary invoice items (not persisted)
            List<InvoiceItem> tempItems = request.getItems().stream().map(itemDto -> {
                InvoiceItem item = new InvoiceItem();
                item.setId(System.currentTimeMillis()); // Temporary ID
                item.setDescription(itemDto.getDescription());
                item.setQuantity(itemDto.getQuantity());
                item.setUnitPrice(itemDto.getUnitPrice());
                item.setTotalPrice(BigDecimal.valueOf(itemDto.getQuantity()).multiply(itemDto.getUnitPrice()));
                item.setTaxAmount(item.getTotalPrice().multiply(BigDecimal.valueOf(0.16))); // 16% tax
                item.setInvoice(tempInvoice);
                return item;
            }).collect(Collectors.toList());

            tempInvoice.setItems(tempItems);

            // Calculate totals
            BigDecimal subtotal = tempItems.stream()
                    .map(InvoiceItem::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal taxAmount = tempItems.stream()
                    .map(InvoiceItem::getTaxAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            tempInvoice.setAmountExcludingTax(subtotal);
            tempInvoice.setTaxAmount(taxAmount);
            tempInvoice.setTotalAmount(subtotal.add(taxAmount));

            // Generate HTML preview using JasperReports service
            String htmlContent = invoiceJasperService.generateInvoicePreviewHtml(tempInvoice);

            return ResponseEntity.ok()
                    .header("Content-Type", "text/html; charset=UTF-8")
                    .body(htmlContent);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la génération de l'aperçu: " + e.getMessage());
        }
    }
}
