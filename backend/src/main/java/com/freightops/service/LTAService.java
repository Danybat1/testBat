package com.freightops.service;

import com.freightops.entity.LTA;
import com.freightops.entity.LTAStatusHistory;
import com.freightops.enums.LTAStatus;
import com.freightops.enums.PaymentMode;
import com.freightops.dto.LTADto;
import com.freightops.dto.LTARequest;
import com.freightops.entity.City;
import com.freightops.entity.Client;
import com.freightops.entity.Tariff;
import com.freightops.repository.CityRepository;
import com.freightops.repository.ClientRepository;
import com.freightops.repository.TariffRepository;
import com.freightops.repository.LTARepository;
import com.freightops.repository.LTAStatusHistoryRepository;
import com.freightops.accounting.service.AccountService;
import com.freightops.accounting.service.FiscalYearService;
import com.freightops.accounting.service.JournalEntryService;
import com.freightops.accounting.entity.Account;
import com.freightops.accounting.entity.JournalEntry;
import com.freightops.accounting.entity.FiscalYear;
import com.freightops.accounting.enums.SourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * LTA Service
 * Business logic for LTA operations
 */
@Service
@Transactional
public class LTAService {

    @Autowired
    private LTARepository ltaRepository;

    @Autowired
    private LTAStatusHistoryRepository ltaStatusHistoryRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private TariffRepository tariffRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private FiscalYearService fiscalYearService;

    @Autowired
    private JournalEntryService journalEntryService;

    private static final Logger logger = Logger.getLogger(LTAService.class.getName());

    /**
     * Create a new LTA from request
     * 
     * @param ltaRequest LTA request object
     * @return created LTA
     */
    public LTA createLTA(LTARequest ltaRequest) {
        // Validate cities exist
        City originCity = cityRepository.findById(ltaRequest.getOriginCityId())
                .orElseThrow(
                        () -> new IllegalArgumentException("Origin city not found: " + ltaRequest.getOriginCityId()));

        City destinationCity = cityRepository.findById(ltaRequest.getDestinationCityId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Destination city not found: " + ltaRequest.getDestinationCityId()));

        // Validate client if required
        Client client = null;
        if (PaymentMode.TO_INVOICE.equals(ltaRequest.getPaymentMode())) {
            if (ltaRequest.getClientId() == null) {
                throw new IllegalArgumentException("Client is required when payment mode is TO_INVOICE");
            }
            client = clientRepository.findById(ltaRequest.getClientId())
                    .orElseThrow(() -> new IllegalArgumentException("Client not found: " + ltaRequest.getClientId()));
        }

        // Convert Request to Entity
        LTA lta = new LTA();
        lta.setLtaNumber(generateLTANumber());
        lta.setTrackingNumber(generateTrackingNumber());
        lta.setOriginCity(originCity);
        lta.setDestinationCity(destinationCity);
        lta.setPaymentMode(ltaRequest.getPaymentMode());
        lta.setClient(client);
        lta.setTotalWeight(ltaRequest.getTotalWeight());
        lta.setPackageNature(ltaRequest.getPackageNature());
        lta.setPackageCount(ltaRequest.getPackageCount());
        lta.setStatus(ltaRequest.getStatus() != null ? ltaRequest.getStatus() : LTAStatus.DRAFT);
        lta.setShipperName(ltaRequest.getShipperName());
        lta.setShipperAddress(ltaRequest.getShipperAddress());
        lta.setConsigneeName(ltaRequest.getConsigneeName());
        lta.setConsigneeAddress(ltaRequest.getConsigneeAddress());
        lta.setSpecialInstructions(ltaRequest.getSpecialInstructions());
        lta.setDeclaredValue(ltaRequest.getDeclaredValue());
        lta.setPickupDate(ltaRequest.getPickupDate());
        lta.setDeliveryDate(ltaRequest.getDeliveryDate());

        // Calculate and set the cost automatically
        Double calculatedCost = calculateCost(
                ltaRequest.getOriginCityId(),
                ltaRequest.getDestinationCityId(),
                ltaRequest.getTotalWeight().doubleValue());
        lta.setCalculatedCost(BigDecimal.valueOf(calculatedCost));

        // Sauvegarder la LTA d'abord pour obtenir l'ID
        LTA savedLTA = ltaRepository.save(lta);

        // Intégration comptable automatique
        integrateAccountingForLTACreation(savedLTA);

        return savedLTA;
    }

    /**
     * Intègre la LTA dans la comptabilité lors de sa création
     * Génère automatiquement les écritures comptables selon les règles de partie
     * double
     * 
     * @param lta LTA créée à intégrer
     */
    private void integrateAccountingForLTACreation(LTA lta) {
        try {
            FiscalYear currentFiscalYear = fiscalYearService.getCurrentFiscalYear();

            if (currentFiscalYear == null) {
                logger.warning("Aucun exercice comptable actuel trouvé pour la LTA " + lta.getId());
                return;
            }

            // Création de l'écriture comptable
            JournalEntry journalEntry = new JournalEntry(
                    LocalDate.now(),
                    "Création LTA " + lta.getLtaNumber() + " - " + getClientName(lta),
                    currentFiscalYear,
                    SourceType.LTA,
                    lta.getId());
            journalEntry.setReference(lta.getLtaNumber());
            journalEntry.setCreatedBy("SYSTEM");

            // Récupération des comptes comptables
            Account clientAccount = accountService.getAccountByNumber("411"); // Compte clients
            Account salesAccount = accountService.getAccountByNumber("701"); // Compte ventes

            if (clientAccount == null || salesAccount == null) {
                logger.warning("Comptes comptables non trouvés pour la création LTA");
                return;
            }

            // Ligne 1: Débit 411 Clients (augmentation de la créance)
            journalEntry.addAccountingEntry(
                    clientAccount,
                    lta.getCalculatedCost(),
                    BigDecimal.ZERO,
                    "Créance LTA " + lta.getLtaNumber() + " - " + getClientName(lta));

            // Ligne 2: Crédit 701 Ventes (augmentation du chiffre d'affaires)
            journalEntry.addAccountingEntry(
                    salesAccount,
                    BigDecimal.ZERO,
                    lta.getCalculatedCost(),
                    "Vente transport aérien LTA " + lta.getLtaNumber());

            // Sauvegarde de l'écriture comptable
            journalEntryService.saveJournalEntry(journalEntry);

            logger.info("Écriture comptable générée avec succès pour la création LTA " + lta.getId());

        } catch (Exception e) {
            logger.severe("Erreur lors de l'intégration comptable de la LTA: " + e.getMessage());
            // Ne pas faire échouer la création de LTA pour une erreur comptable
        }
    }

    /**
     * Récupère le nom du client de la LTA
     */
    private String getClientName(LTA lta) {
        if (lta.getClient() != null && lta.getClient().getName() != null) {
            return lta.getClient().getName();
        }
        return lta.getShipperName() != null ? lta.getShipperName() : "Client inconnu";
    }

    /**
     * Generate unique LTA number
     * 
     * @return unique LTA number
     */
    public String generateLTANumber() {
        String ltaNumber;
        do {
            ltaNumber = "LTA-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (ltaRepository.existsByLtaNumber(ltaNumber));
        return ltaNumber;
    }

    /**
     * Generate unique tracking number
     * 
     * @return unique tracking number
     */
    public String generateTrackingNumber() {
        String trackingNumber;
        do {
            trackingNumber = "TRK-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        } while (false); // Tracking number validation removed as field is not available in entity
        return trackingNumber;
    }

    /**
     * Calculate cost for LTA based on origin, destination and weight
     * 
     * @param originCityId      origin city ID
     * @param destinationCityId destination city ID
     * @param weight            package weight
     * @return calculated cost
     */
    @Transactional(readOnly = true)
    public Double calculateCost(Long originCityId, Long destinationCityId, Double weight) {
        // Find tariff for the route
        Optional<Tariff> tariffOpt = tariffRepository.findByOriginAndDestination(
                originCityId, destinationCityId, LocalDateTime.now());

        if (tariffOpt.isPresent()) {
            Tariff tariff = tariffOpt.get();
            BigDecimal weightBD = BigDecimal.valueOf(weight);
            BigDecimal cost = tariff.getKgRate().multiply(weightBD);
            return cost.doubleValue();
        } else {
            // Default rate if no tariff found (fallback)
            return weight * 2.0; // Default $2.0 per kg
        }
    }

    /**
     * Get LTA by ID
     * 
     * @param id LTA ID
     * @return Optional LTA
     */
    @Transactional(readOnly = true)
    public Optional<LTA> getLTAById(Long id) {
        return ltaRepository.findById(id);
    }

    /**
     * Get LTA by LTA number
     * 
     * @param ltaNumber LTA number
     * @return Optional LTA
     */
    @Transactional(readOnly = true)
    public Optional<LTA> getLTAByNumber(String ltaNumber) {
        return ltaRepository.findByLtaNumber(ltaNumber);
    }

    /**
     * Get LTA by tracking number
     * 
     * @param trackingNumber tracking number
     * @return Optional LTA
     */
    @Transactional(readOnly = true)
    public Optional<LTA> getLTAByTrackingNumber(String trackingNumber) {
        return ltaRepository.findByTrackingNumber(trackingNumber);
    }

    /**
     * Get all LTAs with pagination
     * 
     * @param pageable pagination information
     * @return Page of LTAs
     */
    @Transactional(readOnly = true)
    public Page<LTA> getAllLTAs(Pageable pageable) {
        Page<LTA> ltas = ltaRepository.findAll(pageable);
        // Calculate cost for LTAs that don't have it calculated
        ltas.getContent().forEach(this::ensureCostCalculated);
        return ltas;
    }

    /**
     * Search LTAs by criteria
     * 
     * @param status    status filter (optional)
     * @param shipper   shipper filter (optional)
     * @param consignee consignee filter (optional)
     * @param pageable  pagination information
     * @return Page of LTAs
     */
    @Transactional(readOnly = true)
    public Page<LTA> searchLTAs(LTAStatus status, String shipper, String consignee, Pageable pageable) {
        Page<LTA> ltas = ltaRepository.findByMultipleCriteria(status, shipper, consignee, pageable);
        // Calculate cost for LTAs that don't have it calculated
        ltas.getContent().forEach(this::ensureCostCalculated);
        return ltas;
    }

    /**
     * Update LTA status and regenerate tracking number if needed
     * 
     * @param id     LTA ID
     * @param status new status
     * @return updated LTA
     */
    public Optional<LTA> updateLTAStatus(Long id, LTAStatus status) {
        logger.info("updateLTAStatus called for LTA ID: " + id + " with new status: " + status);

        Optional<LTA> ltaOpt = ltaRepository.findById(id);
        if (ltaOpt.isPresent()) {
            LTA lta = ltaOpt.get();
            LTAStatus oldStatus = lta.getStatus();
            logger.info("Current LTA status: " + oldStatus + ", tracking number: " + lta.getTrackingNumber());

            lta.setStatus(status);

            // Regenerate tracking number when status changes from DRAFT to CONFIRMED
            if (oldStatus == LTAStatus.DRAFT && status == LTAStatus.CONFIRMED) {
                logger.info("Status change detected: DRAFT -> CONFIRMED, but keeping existing tracking number");
                // Keep the existing tracking number - no regeneration needed
                // The tracking number was already generated at creation time
            }

            // Generate QR code when status becomes CONFIRMED or IN_TRANSIT
            if (status == LTAStatus.CONFIRMED || status == LTAStatus.IN_TRANSIT) {
                logger.info("Generating QR code for status: " + status);
                generateQRCodeForLTA(lta);
            }

            LTA savedLTA = ltaRepository.save(lta);
            logger.info("LTA saved successfully with tracking number: " + savedLTA.getTrackingNumber());
            return Optional.of(savedLTA);
        } else {
            logger.warning("LTA not found with ID: " + id);
        }
        return Optional.empty();
    }

    /**
     * Update LTA
     * 
     * @param id     LTA ID
     * @param ltaDto updated LTA data
     * @return updated LTA
     */
    public Optional<LTA> updateLTA(Long id, LTADto ltaDto) {
        Optional<LTA> ltaOpt = ltaRepository.findById(id);
        if (ltaOpt.isPresent()) {
            LTA lta = ltaOpt.get();

            // Check unique constraints if values are being changed
            if (!lta.getLtaNumber().equals(ltaDto.getLtaNumber()) &&
                    ltaRepository.existsByLtaNumber(ltaDto.getLtaNumber())) {
                throw new IllegalArgumentException("LTA number already exists: " + ltaDto.getLtaNumber());
            }

            // Update fields
            lta.setLtaNumber(ltaDto.getLtaNumber());
            lta.setStatus(ltaDto.getStatus());
            lta.setShipperName(ltaDto.getShipper());
            lta.setConsigneeName(ltaDto.getConsignee());
            lta.setTotalWeight(ltaDto.getWeight());
            lta.setDeclaredValue(ltaDto.getDeclaredValue());

            return Optional.of(ltaRepository.save(lta));
        }
        return Optional.empty();
    }

    /**
     * Delete LTA
     * 
     * @param id LTA ID
     * @return true if deleted, false if not found
     */
    public boolean deleteLTA(Long id) {
        if (ltaRepository.existsById(id)) {
            ltaRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Get LTA count by status
     * 
     * @param status LTA status
     * @return count
     */
    @Transactional(readOnly = true)
    public long getCountByStatus(LTAStatus status) {
        return ltaRepository.countByStatus(status);
    }

    /**
     * Ensure cost is calculated for an LTA
     * 
     * @param lta LTA to ensure cost calculation
     */
    private void ensureCostCalculated(LTA lta) {
        if (lta.getCalculatedCost() == null && lta.getOriginCity() != null &&
                lta.getDestinationCity() != null && lta.getTotalWeight() != null) {
            Double calculatedCost = calculateCost(
                    lta.getOriginCity().getId(),
                    lta.getDestinationCity().getId(),
                    lta.getTotalWeight().doubleValue());
            lta.setCalculatedCost(BigDecimal.valueOf(calculatedCost));
        }
    }

    /**
     * Generate QR code for LTA tracking
     * 
     * @param lta LTA entity
     */
    private void generateQRCodeForLTA(LTA lta) {
        if (lta.getTrackingNumber() != null && !lta.getTrackingNumber().isEmpty()) {
            // Generate QR code data with public tracking URL
            String qrData = "http://localhost:4201/tracking?number=" + lta.getTrackingNumber();

            // Store QR code in the LTA entity
            lta.setQrCode(qrData);
            logger.info("Generated QR code for LTA " + lta.getLtaNumber() + ": " + qrData);
        }
    }

    /**
     * Get status history by tracking number
     * 
     * @param trackingNumber tracking number
     * @return List of LTAStatusHistory
     */
    @Transactional(readOnly = true)
    public List<LTAStatusHistory> getStatusHistoryByTrackingNumber(String trackingNumber) {
        return ltaStatusHistoryRepository.findByLtaTrackingNumberOrderByChangedAtAsc(trackingNumber);
    }
}
