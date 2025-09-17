package com.freightops.service;

import com.freightops.entity.Tax;
import com.freightops.repository.TaxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TaxService {

    @Autowired
    private TaxRepository taxRepository;

    public List<Tax> getAllTaxes() {
        return taxRepository.findAll();
    }

    public List<Tax> getActiveTaxes() {
        return taxRepository.findActiveTaxesOrderByName();
    }

    public Optional<Tax> getTaxById(Long id) {
        return taxRepository.findById(id);
    }

    public Tax getTaxByName(String name) {
        return taxRepository.findByNameIgnoreCase(name);
    }

    public Tax createTax(Tax tax) {
        if (taxRepository.existsByNameIgnoreCase(tax.getName())) {
            throw new RuntimeException("Une taxe avec ce nom existe déjà");
        }
        return taxRepository.save(tax);
    }

    public Tax updateTax(Long id, Tax taxDetails) {
        Tax tax = taxRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Taxe non trouvée avec l'ID: " + id));

        // Vérifier si le nom existe déjà pour une autre taxe
        Tax existingTax = taxRepository.findByNameIgnoreCase(taxDetails.getName());
        if (existingTax != null && !existingTax.getId().equals(id)) {
            throw new RuntimeException("Une taxe avec ce nom existe déjà");
        }

        tax.setName(taxDetails.getName());
        tax.setRate(taxDetails.getRate());
        tax.setDescription(taxDetails.getDescription());
        tax.setActive(taxDetails.getActive());

        return taxRepository.save(tax);
    }

    public void deleteTax(Long id) {
        Tax tax = taxRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Taxe non trouvée avec l'ID: " + id));
        
        // Désactiver plutôt que supprimer pour préserver l'intégrité des données
        tax.setActive(false);
        taxRepository.save(tax);
    }

    public void activateTax(Long id) {
        Tax tax = taxRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Taxe non trouvée avec l'ID: " + id));
        tax.setActive(true);
        taxRepository.save(tax);
    }

    public void deactivateTax(Long id) {
        Tax tax = taxRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Taxe non trouvée avec l'ID: " + id));
        tax.setActive(false);
        taxRepository.save(tax);
    }
}
