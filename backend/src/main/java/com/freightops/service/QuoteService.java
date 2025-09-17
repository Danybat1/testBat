package com.freightops.service;

import com.freightops.entity.Quote;
import com.freightops.entity.QuoteItem;
import com.freightops.entity.Client;
import com.freightops.entity.Invoice;
import com.freightops.entity.InvoiceItem;
import com.freightops.repository.QuoteRepository;
import com.freightops.repository.QuoteItemRepository;
import com.freightops.repository.ClientRepository;
import com.freightops.enums.InvoiceType;
import com.freightops.enums.InvoiceStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.Year;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class QuoteService {

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private QuoteItemRepository quoteItemRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private InvoiceService invoiceService;

    public List<Quote> getAllQuotes() {
        return quoteRepository.findAll();
    }

    public List<Quote> getQuotesByClient(Long clientId) {
        return quoteRepository.findByClientIdOrderByQuoteDateDesc(clientId);
    }

    public List<Quote> getQuotesByStatus(InvoiceStatus status) {
        return quoteRepository.findByStatus(status);
    }

    public List<Quote> getUnconvertedQuotes() {
        return quoteRepository.findByConvertedFalse();
    }

    public List<Quote> getExpiredQuotes() {
        return quoteRepository.findExpiredQuotes(LocalDate.now());
    }

    public List<Quote> getQuotesByDateRange(LocalDate startDate, LocalDate endDate) {
        return quoteRepository.findByQuoteDateBetween(startDate, endDate);
    }

    public Optional<Quote> getQuoteById(Long id) {
        return quoteRepository.findById(id);
    }

    public Optional<Quote> getQuoteByNumber(String quoteNumber) {
        return quoteRepository.findByQuoteNumber(quoteNumber);
    }

    public Quote createQuote(Quote quote) {
        // Générer le numéro de devis automatiquement
        if (quote.getQuoteNumber() == null || quote.getQuoteNumber().isEmpty()) {
            quote.setQuoteNumber(generateQuoteNumber());
        }

        // Vérifier que le client existe
        Client client = clientRepository.findById(quote.getClient().getId())
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
        quote.setClient(client);

        // Calculer les montants
        calculateQuoteAmounts(quote);

        return quoteRepository.save(quote);
    }

    public Quote updateQuote(Long id, Quote quoteDetails) {
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Devis non trouvé avec l'ID: " + id));

        if (quote.getConverted()) {
            throw new RuntimeException("Impossible de modifier un devis déjà converti en facture");
        }

        quote.setQuoteDate(quoteDetails.getQuoteDate());
        quote.setValidUntil(quoteDetails.getValidUntil());
        quote.setDescription(quoteDetails.getDescription());
        quote.setAmountExcludingTax(quoteDetails.getAmountExcludingTax());
        quote.setTaxAmount(quoteDetails.getTaxAmount());
        quote.setStatus(quoteDetails.getStatus());

        calculateQuoteAmounts(quote);

        return quoteRepository.save(quote);
    }

    public void deleteQuote(Long id) {
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Devis non trouvé avec l'ID: " + id));

        if (quote.getConverted()) {
            throw new RuntimeException("Impossible de supprimer un devis déjà converti en facture");
        }

        quoteRepository.delete(quote);
    }

    public Quote addItemToQuote(Long quoteId, QuoteItem item) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new RuntimeException("Devis non trouvé avec l'ID: " + quoteId));

        if (quote.getConverted()) {
            throw new RuntimeException("Impossible de modifier un devis déjà converti en facture");
        }

        item.setQuote(quote);
        quoteItemRepository.save(item);

        // Recalculer les montants du devis
        recalculateQuoteAmounts(quote);

        return quoteRepository.save(quote);
    }

    public Quote removeItemFromQuote(Long quoteId, Long itemId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new RuntimeException("Devis non trouvé avec l'ID: " + quoteId));

        if (quote.getConverted()) {
            throw new RuntimeException("Impossible de modifier un devis déjà converti en facture");
        }

        QuoteItem item = quoteItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Article non trouvé avec l'ID: " + itemId));

        if (!item.getQuote().getId().equals(quoteId)) {
            throw new RuntimeException("Cet article n'appartient pas à ce devis");
        }

        quoteItemRepository.delete(item);

        // Recalculer les montants du devis
        recalculateQuoteAmounts(quote);

        return quoteRepository.save(quote);
    }

    public Invoice convertQuoteToInvoice(Long quoteId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new RuntimeException("Devis non trouvé avec l'ID: " + quoteId));

        if (quote.getConverted()) {
            throw new RuntimeException("Ce devis a déjà été converti en facture");
        }

        if (quote.getValidUntil().isBefore(LocalDate.now())) {
            throw new RuntimeException("Ce devis a expiré et ne peut plus être converti");
        }

        // Créer la facture
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(invoiceService.generateInvoiceNumber());
        invoice.setType(InvoiceType.CLIENT);
        invoice.setClient(quote.getClient());
        invoice.setQuote(quote);
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setDueDate(LocalDate.now().plusDays(30)); // 30 jours par défaut
        invoice.setDescription(quote.getDescription());
        invoice.setAmountExcludingTax(quote.getAmountExcludingTax());
        invoice.setTaxAmount(quote.getTaxAmount());
        invoice.setStatus(InvoiceStatus.DRAFT);

        // Copier les articles du devis vers la facture
        for (QuoteItem quoteItem : quote.getItems()) {
            InvoiceItem invoiceItem = new InvoiceItem();
            invoiceItem.setInvoice(invoice);
            invoiceItem.setDescription(quoteItem.getDescription());
            invoiceItem.setQuantity(quoteItem.getQuantity());
            invoiceItem.setUnitPrice(quoteItem.getUnitPrice());
            invoice.getItems().add(invoiceItem);
        }

        // Sauvegarder la facture
        Invoice savedInvoice = invoiceService.createInvoice(invoice);

        // Marquer le devis comme converti
        quote.setConverted(true);
        quote.setStatus(InvoiceStatus.SENT);
        quoteRepository.save(quote);

        return savedInvoice;
    }

    private void calculateQuoteAmounts(Quote quote) {
        if (quote.getAmountExcludingTax() != null) {
            if (quote.getTaxAmount() == null) {
                quote.setTaxAmount(BigDecimal.ZERO);
            }
            quote.setTotalAmount(quote.getAmountExcludingTax().add(quote.getTaxAmount()));
        }
    }

    private void recalculateQuoteAmounts(Quote quote) {
        List<QuoteItem> items = quoteItemRepository.findByQuote(quote);
        
        BigDecimal totalExcludingTax = items.stream()
                .map(QuoteItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        quote.setAmountExcludingTax(totalExcludingTax);
        calculateQuoteAmounts(quote);
    }

    private String generateQuoteNumber() {
        int currentYear = Year.now().getValue();
        Long count = quoteRepository.countByYear(currentYear);
        return String.format("DEV-%d-%05d", currentYear, count + 1);
    }
}
