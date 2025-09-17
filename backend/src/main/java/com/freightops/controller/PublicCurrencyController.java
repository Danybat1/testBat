package com.freightops.controller;

import com.freightops.entity.Currency;
import com.freightops.entity.ExchangeRate;
import com.freightops.service.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public Currency Controller - endpoints without /api prefix
 * For frontend direct access to currency data
 */
@RestController
@RequestMapping("/currencies")
@CrossOrigin(origins = "*")
public class PublicCurrencyController {

    @Autowired
    private CurrencyService currencyService;

    /**
     * Get all active currencies
     * GET /currencies
     */
    @GetMapping
    public ResponseEntity<List<Currency>> getAllCurrencies() {
        List<Currency> currencies = currencyService.getAllActiveCurrencies();
        return ResponseEntity.ok(currencies);
    }

    /**
     * Get all exchange rates
     * GET /currencies/rates
     */
    @GetMapping("/rates")
    public ResponseEntity<List<ExchangeRate>> getAllExchangeRates() {
        List<ExchangeRate> rates = currencyService.getAllActiveExchangeRates();
        return ResponseEntity.ok(rates);
    }

    /**
     * Get default currency
     * GET /currencies/default
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
     * Get currency by code
     * GET /currencies/{code}
     */
    @GetMapping("/{code}")
    public ResponseEntity<Currency> getCurrencyByCode(@PathVariable String code) {
        return currencyService.getCurrencyByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
