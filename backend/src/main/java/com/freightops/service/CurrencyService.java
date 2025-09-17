package com.freightops.service;

import com.freightops.entity.Currency;
import com.freightops.entity.ExchangeRate;
import com.freightops.repository.CurrencyRepository;
import com.freightops.repository.ExchangeRateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CurrencyService {

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    /**
     * Obtenir toutes les devises actives
     */
    @Cacheable("currencies")
    public List<Currency> getAllActiveCurrencies() {
        return currencyRepository.findByIsActiveTrueOrderByCodeAsc();
    }

    /**
     * Obtenir la devise par défaut
     */
    @Cacheable("defaultCurrency")
    public Currency getDefaultCurrency() {
        return currencyRepository.findByIsDefaultTrue()
                .orElseGet(() -> {
                    // Si aucune devise par défaut, retourner USD
                    return currencyRepository.findByCodeAndIsActiveTrue("USD")
                            .orElse(null);
                });
    }

    /**
     * Obtenir une devise par son code
     */
    @Cacheable(value = "currency", key = "#code")
    public Optional<Currency> getCurrencyByCode(String code) {
        return currencyRepository.findByCodeAndIsActiveTrue(code);
    }

    /**
     * Convertir un montant d'une devise à une autre
     */
    public BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return amount;
        }

        BigDecimal rate = getExchangeRate(fromCurrency, toCurrency);
        if (rate == null) {
            throw new RuntimeException("Taux de change non disponible pour " + fromCurrency + " vers " + toCurrency);
        }

        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Obtenir le taux de change entre deux devises
     */
    @Cacheable(value = "exchangeRate", key = "#fromCurrency + '_' + #toCurrency")
    public BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return BigDecimal.ONE;
        }

        Optional<ExchangeRate> rate = exchangeRateRepository.findCurrentRate(fromCurrency, toCurrency);
        if (rate.isPresent()) {
            return rate.get().getRate();
        }

        // Essayer le taux inverse
        Optional<ExchangeRate> inverseRate = exchangeRateRepository.findCurrentRate(toCurrency, fromCurrency);
        if (inverseRate.isPresent()) {
            return BigDecimal.ONE.divide(inverseRate.get().getRate(), 6, RoundingMode.HALF_UP);
        }

        return null;
    }

    /**
     * Mettre à jour un taux de change
     */
    @CacheEvict(value = { "exchangeRate", "exchangeRates" }, allEntries = true)
    public ExchangeRate updateExchangeRate(String fromCurrency, String toCurrency,
            BigDecimal newRate, String updatedBy) {
        Currency from = getCurrencyByCode(fromCurrency)
                .orElseThrow(() -> new RuntimeException("Devise source non trouvée: " + fromCurrency));
        Currency to = getCurrencyByCode(toCurrency)
                .orElseThrow(() -> new RuntimeException("Devise cible non trouvée: " + toCurrency));

        // Désactiver l'ancien taux s'il existe
        Optional<ExchangeRate> existingRate = exchangeRateRepository.findCurrentRate(fromCurrency, toCurrency);
        if (existingRate.isPresent()) {
            ExchangeRate oldRate = existingRate.get();
            oldRate.setIsActive(false);
            exchangeRateRepository.save(oldRate);
        }

        // Créer le nouveau taux
        ExchangeRate newExchangeRate = new ExchangeRate();
        newExchangeRate.setFromCurrency(from);
        newExchangeRate.setToCurrency(to);
        newExchangeRate.setRate(newRate);
        newExchangeRate.setCreatedBy(updatedBy);
        newExchangeRate.setEffectiveDate(LocalDateTime.now());

        return exchangeRateRepository.save(newExchangeRate);
    }

    /**
     * Obtenir tous les taux de change actifs
     */
    @Cacheable("exchangeRates")
    public List<ExchangeRate> getAllActiveExchangeRates() {
        return exchangeRateRepository.findByIsActiveTrueOrderByEffectiveDateDesc();
    }

    /**
     * Formater un montant selon la devise
     */
    public String formatAmount(BigDecimal amount, String currencyCode) {
        Currency currency = getCurrencyByCode(currencyCode).orElse(null);
        if (currency == null) {
            return amount.toString();
        }

        int scale = currency.getDecimalPlaces();
        BigDecimal scaledAmount = amount.setScale(scale, RoundingMode.HALF_UP);

        switch (currencyCode) {
            case "USD":
                return "$" + String.format("%,.2f", scaledAmount);
            case "CDF":
                return String.format("%,.0f", scaledAmount) + " FC";
            default:
                return currency.getSymbol() + String.format("%,." + scale + "f", scaledAmount);
        }
    }

    /**
     * Convertir et formater un montant
     */
    public String convertAndFormat(BigDecimal amount, String fromCurrency, String toCurrency) {
        BigDecimal convertedAmount = convert(amount, fromCurrency, toCurrency);
        return formatAmount(convertedAmount, toCurrency);
    }

    /**
     * Initialiser les données par défaut
     */
    @CacheEvict(value = { "currencies", "defaultCurrency", "exchangeRate", "exchangeRates" }, allEntries = true)
    public void initializeDefaultData() {
        // Créer les devises par défaut si elles n'existent pas
        if (!currencyRepository.existsByCodeAndIsActiveTrue("USD")) {
            Currency usd = new Currency("USD", "Dollar Américain", "$", 2);
            usd.setIsDefault(true);
            currencyRepository.save(usd);
        }

        if (!currencyRepository.existsByCodeAndIsActiveTrue("CDF")) {
            Currency cdf = new Currency("CDF", "Franc Congolais", "FC", 0);
            currencyRepository.save(cdf);
        }

        // Créer les taux de change par défaut
        if (!exchangeRateRepository.existsByFromCurrency_CodeAndToCurrency_CodeAndIsActiveTrue("USD", "CDF")) {
            Currency usd = currencyRepository.findByCodeAndIsActiveTrue("USD").get();
            Currency cdf = currencyRepository.findByCodeAndIsActiveTrue("CDF").get();

            ExchangeRate usdToCdf = new ExchangeRate(usd, cdf, new BigDecimal("2700.00"));
            usdToCdf.setCreatedBy("SYSTEM");
            exchangeRateRepository.save(usdToCdf);

            ExchangeRate cdfToUsd = new ExchangeRate(cdf, usd, new BigDecimal("0.000370"));
            cdfToUsd.setCreatedBy("SYSTEM");
            exchangeRateRepository.save(cdfToUsd);
        }
    }
}
