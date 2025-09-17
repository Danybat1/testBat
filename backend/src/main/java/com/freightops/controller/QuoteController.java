package com.freightops.controller;

import com.freightops.entity.Quote;
import com.freightops.entity.QuoteItem;
import com.freightops.entity.Invoice;
import com.freightops.service.QuoteService;
import com.freightops.enums.InvoiceStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/quotes")
@CrossOrigin(origins = "*")
public class QuoteController {

    @Autowired
    private QuoteService quoteService;

    @GetMapping
    public ResponseEntity<List<Quote>> getAllQuotes() {
        List<Quote> quotes = quoteService.getAllQuotes();
        return ResponseEntity.ok(quotes);
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<Quote>> getQuotesByClient(@PathVariable Long clientId) {
        List<Quote> quotes = quoteService.getQuotesByClient(clientId);
        return ResponseEntity.ok(quotes);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Quote>> getQuotesByStatus(@PathVariable InvoiceStatus status) {
        List<Quote> quotes = quoteService.getQuotesByStatus(status);
        return ResponseEntity.ok(quotes);
    }

    @GetMapping("/unconverted")
    public ResponseEntity<List<Quote>> getUnconvertedQuotes() {
        List<Quote> quotes = quoteService.getUnconvertedQuotes();
        return ResponseEntity.ok(quotes);
    }

    @GetMapping("/expired")
    public ResponseEntity<List<Quote>> getExpiredQuotes() {
        List<Quote> quotes = quoteService.getExpiredQuotes();
        return ResponseEntity.ok(quotes);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<Quote>> getQuotesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Quote> quotes = quoteService.getQuotesByDateRange(startDate, endDate);
        return ResponseEntity.ok(quotes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Quote> getQuoteById(@PathVariable Long id) {
        return quoteService.getQuoteById(id)
                .map(quote -> ResponseEntity.ok(quote))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{quoteNumber}")
    public ResponseEntity<Quote> getQuoteByNumber(@PathVariable String quoteNumber) {
        return quoteService.getQuoteByNumber(quoteNumber)
                .map(quote -> ResponseEntity.ok(quote))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createQuote(@Valid @RequestBody Quote quote) {
        try {
            Quote createdQuote = quoteService.createQuote(quote);
            return ResponseEntity.ok(createdQuote);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateQuote(@PathVariable Long id, @Valid @RequestBody Quote quoteDetails) {
        try {
            Quote updatedQuote = quoteService.updateQuote(id, quoteDetails);
            return ResponseEntity.ok(updatedQuote);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteQuote(@PathVariable Long id) {
        try {
            quoteService.deleteQuote(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<?> addItemToQuote(@PathVariable Long id, @Valid @RequestBody QuoteItem item) {
        try {
            Quote updatedQuote = quoteService.addItemToQuote(id, item);
            return ResponseEntity.ok(updatedQuote);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{quoteId}/items/{itemId}")
    public ResponseEntity<?> removeItemFromQuote(@PathVariable Long quoteId, @PathVariable Long itemId) {
        try {
            Quote updatedQuote = quoteService.removeItemFromQuote(quoteId, itemId);
            return ResponseEntity.ok(updatedQuote);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/convert")
    public ResponseEntity<?> convertQuoteToInvoice(@PathVariable Long id) {
        try {
            Invoice invoice = quoteService.convertQuoteToInvoice(id);
            return ResponseEntity.ok(invoice);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
