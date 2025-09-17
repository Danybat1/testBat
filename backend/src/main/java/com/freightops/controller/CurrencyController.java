package com.freightops.controller;

import com.freightops.entity.Currency;
import com.freightops.entity.ExchangeRate;
import com.freightops.service.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/currencies")
@CrossOrigin(origins = "*")
public class CurrencyController {

    @Autowired
    private CurrencyService currencyService;

    /**
     * Obtenir toutes les devises actives
     */
    @GetMapping
    public ResponseEntity<List<Currency>> getAllCurrencies() {
        List<Currency> currencies = currencyService.getAllActiveCurrencies();
        return ResponseEntity.ok(currencies);
    }

    /**
     * Obtenir la devise par défaut
     */
    @GetMapping("/default")
    public ResponseEntity<Currency> getDefaultCurrency() {
        Currency defaultCurrency = currencyService.getDefaultCurrency();
        if (defaultCurrency != null) {
            return ResponseEntity.ok(defaultCurrency);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Obtenir une devise par son code
     */
    @GetMapping("/{code}")
    public ResponseEntity<Currency> getCurrencyByCode(@PathVariable String code) {
        return currencyService.getCurrencyByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Convertir un montant entre deux devises
     */
    @PostMapping("/convert")
    public ResponseEntity<Map<String, Object>> convertAmount(
            @RequestBody Map<String, Object> request) {

        try {
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String fromCurrency = request.get("fromCurrency").toString();
            String toCurrency = request.get("toCurrency").toString();

            BigDecimal convertedAmount = currencyService.convert(amount, fromCurrency, toCurrency);
            BigDecimal rate = currencyService.getExchangeRate(fromCurrency, toCurrency);

            Map<String, Object> response = Map.of(
                    "originalAmount", amount,
                    "convertedAmount", convertedAmount,
                    "fromCurrency", fromCurrency,
                    "toCurrency", toCurrency,
                    "exchangeRate", rate,
                    "formattedAmount", currencyService.formatAmount(convertedAmount, toCurrency));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Obtenir le taux de change entre deux devises
     */
    @GetMapping("/rate/{from}/{to}")
    public ResponseEntity<Map<String, Object>> getExchangeRate(
            @PathVariable String from, @PathVariable String to) {

        BigDecimal rate = currencyService.getExchangeRate(from, to);
        if (rate != null) {
            Map<String, Object> response = Map.of(
                    "fromCurrency", from,
                    "toCurrency", to,
                    "rate", rate,
                    "pair", from + "/" + to);
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Obtenir tous les taux de change actifs
     */
    @GetMapping("/rates")
    public ResponseEntity<List<ExchangeRate>> getAllExchangeRates() {
        List<ExchangeRate> rates = currencyService.getAllActiveExchangeRates();
        return ResponseEntity.ok(rates);
    }

    /**
     * Mettre à jour un taux de change (Admin seulement)
     */
    @PutMapping("/rates")
    public ResponseEntity<ExchangeRate> updateExchangeRate(
            @RequestBody Map<String, Object> request) {

        try {
            String fromCurrency = request.get("fromCurrency").toString();
            String toCurrency = request.get("toCurrency").toString();
            BigDecimal newRate = new BigDecimal(request.get("rate").toString());
            String updatedBy = request.getOrDefault("updatedBy", "ADMIN").toString();

            ExchangeRate updatedRate = currencyService.updateExchangeRate(
                    fromCurrency, toCurrency, newRate, updatedBy);

            return ResponseEntity.ok(updatedRate);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Formater un montant selon une devise
     */
    @PostMapping("/format")
    public ResponseEntity<Map<String, String>> formatAmount(
            @RequestBody Map<String, Object> request) {

        try {
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String currency = request.get("currency").toString();

            String formattedAmount = currencyService.formatAmount(amount, currency);

            return ResponseEntity.ok(Map.of(
                    "amount", amount.toString(),
                    "currency", currency,
                    "formatted", formattedAmount));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Initialiser les données par défaut (Dev/Test seulement)
     */
    @PostMapping("/initialize")
    public ResponseEntity<Map<String, String>> initializeDefaultData() {
        try {
            currencyService.initializeDefaultData();
            return ResponseEntity.ok(Map.of("message", "Données par défaut initialisées avec succès"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
