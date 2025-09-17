package com.freightops.controller;

import com.freightops.entity.Tax;
import com.freightops.service.TaxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/taxes")
@CrossOrigin(origins = "*")
public class TaxController {

    @Autowired
    private TaxService taxService;

    @GetMapping
    public ResponseEntity<List<Tax>> getAllTaxes() {
        List<Tax> taxes = taxService.getAllTaxes();
        return ResponseEntity.ok(taxes);
    }

    @GetMapping("/active")
    public ResponseEntity<List<Tax>> getActiveTaxes() {
        List<Tax> taxes = taxService.getActiveTaxes();
        return ResponseEntity.ok(taxes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tax> getTaxById(@PathVariable Long id) {
        return taxService.getTaxById(id)
                .map(tax -> ResponseEntity.ok(tax))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Tax> getTaxByName(@PathVariable String name) {
        Tax tax = taxService.getTaxByName(name);
        return tax != null ? ResponseEntity.ok(tax) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<?> createTax(@Valid @RequestBody Tax tax) {
        try {
            Tax createdTax = taxService.createTax(tax);
            return ResponseEntity.ok(createdTax);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTax(@PathVariable Long id, @Valid @RequestBody Tax taxDetails) {
        try {
            Tax updatedTax = taxService.updateTax(id, taxDetails);
            return ResponseEntity.ok(updatedTax);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTax(@PathVariable Long id) {
        try {
            taxService.deleteTax(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateTax(@PathVariable Long id) {
        try {
            taxService.activateTax(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateTax(@PathVariable Long id) {
        try {
            taxService.deactivateTax(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
