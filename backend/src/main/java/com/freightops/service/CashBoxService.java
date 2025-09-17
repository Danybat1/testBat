package com.freightops.service;

import com.freightops.entity.CashBox;
import com.freightops.entity.TreasuryTransaction;
import com.freightops.repository.CashBoxRepository;
import com.freightops.repository.TreasuryTransactionRepository;
import com.freightops.enums.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CashBoxService {

    @Autowired
    private CashBoxRepository cashBoxRepository;

    @Autowired
    private TreasuryTransactionRepository treasuryTransactionRepository;

    public List<CashBox> getAllCashBoxes() {
        return cashBoxRepository.findAll();
    }

    public List<CashBox> getActiveCashBoxes() {
        return cashBoxRepository.findActiveCashBoxesOrderByName();
    }

    public Optional<CashBox> getCashBoxById(Long id) {
        return cashBoxRepository.findById(id);
    }

    public CashBox getCashBoxByName(String name) {
        return cashBoxRepository.findByNameIgnoreCase(name);
    }

    public BigDecimal getTotalCashBalance() {
        BigDecimal total = cashBoxRepository.sumCurrentBalanceOfActiveCashBoxes();
        return total != null ? total : BigDecimal.ZERO;
    }

    public List<CashBox> getCashBoxesWithLowBalance(BigDecimal threshold) {
        return cashBoxRepository.findCashBoxesWithLowBalance(threshold);
    }

    public CashBox createCashBox(CashBox cashBox) {
        if (cashBoxRepository.existsByNameIgnoreCase(cashBox.getName())) {
            throw new RuntimeException("Une caisse avec ce nom existe déjà");
        }

        if (cashBox.getCurrentBalance() == null) {
            cashBox.setCurrentBalance(cashBox.getInitialBalance());
        }

        return cashBoxRepository.save(cashBox);
    }

    public CashBox updateCashBox(Long id, CashBox cashBoxDetails) {
        CashBox cashBox = cashBoxRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Caisse non trouvée avec l'ID: " + id));

        // Vérifier si le nom existe déjà pour une autre caisse
        CashBox existingCashBox = cashBoxRepository.findByNameIgnoreCase(cashBoxDetails.getName());
        if (existingCashBox != null && !existingCashBox.getId().equals(id)) {
            throw new RuntimeException("Une caisse avec ce nom existe déjà");
        }

        cashBox.setName(cashBoxDetails.getName());
        cashBox.setDescription(cashBoxDetails.getDescription());
        cashBox.setActive(cashBoxDetails.getActive());

        return cashBoxRepository.save(cashBox);
    }

    public void deleteCashBox(Long id) {
        CashBox cashBox = cashBoxRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Caisse non trouvée avec l'ID: " + id));

        // Vérifier s'il y a des transactions
        List<TreasuryTransaction> transactions = treasuryTransactionRepository.findByCashBox(cashBox);
        if (!transactions.isEmpty()) {
            throw new RuntimeException("Impossible de supprimer une caisse avec des transactions. Désactivez-la plutôt.");
        }

        cashBoxRepository.delete(cashBox);
    }

    public CashBox activateCashBox(Long id) {
        CashBox cashBox = cashBoxRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Caisse non trouvée avec l'ID: " + id));
        cashBox.setActive(true);
        return cashBoxRepository.save(cashBox);
    }

    public CashBox deactivateCashBox(Long id) {
        CashBox cashBox = cashBoxRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Caisse non trouvée avec l'ID: " + id));
        cashBox.setActive(false);
        return cashBoxRepository.save(cashBox);
    }

    public CashBox adjustBalance(Long id, BigDecimal amount, String reason) {
        CashBox cashBox = cashBoxRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Caisse non trouvée avec l'ID: " + id));

        if (!cashBox.getActive()) {
            throw new RuntimeException("Impossible d'ajuster le solde d'une caisse inactive");
        }

        BigDecimal newBalance = cashBox.getCurrentBalance().add(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Le solde ne peut pas être négatif");
        }

        cashBox.setCurrentBalance(newBalance);
        return cashBoxRepository.save(cashBox);
    }

    public BigDecimal getCashBoxBalance(Long id, LocalDate date) {
        CashBox cashBox = cashBoxRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Caisse non trouvée avec l'ID: " + id));

        // Calculer le solde à une date donnée
        BigDecimal income = treasuryTransactionRepository.sumByCashBoxAndTypeAndDateRange(
                id, TransactionType.INCOME, LocalDate.MIN, date);
        BigDecimal expense = treasuryTransactionRepository.sumByCashBoxAndTypeAndDateRange(
                id, TransactionType.EXPENSE, LocalDate.MIN, date);

        if (income == null) income = BigDecimal.ZERO;
        if (expense == null) expense = BigDecimal.ZERO;

        return cashBox.getInitialBalance().add(income).subtract(expense);
    }

    public List<TreasuryTransaction> getCashBoxTransactions(Long id) {
        CashBox cashBox = cashBoxRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Caisse non trouvée avec l'ID: " + id));
        return treasuryTransactionRepository.findByCashBoxIdOrderByTransactionDateDesc(id);
    }

    public List<TreasuryTransaction> getCashBoxTransactionsByDateRange(Long id, LocalDate startDate, LocalDate endDate) {
        CashBox cashBox = cashBoxRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Caisse non trouvée avec l'ID: " + id));
        
        return treasuryTransactionRepository.findByCashBox(cashBox).stream()
                .filter(t -> !t.getTransactionDate().isBefore(startDate) && !t.getTransactionDate().isAfter(endDate))
                .toList();
    }
}
