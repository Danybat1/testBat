package com.freightops.accounting.service;

import com.freightops.accounting.entity.Account;
import com.freightops.accounting.enums.AccountType;
import com.freightops.accounting.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service pour la gestion du plan comptable
 * Gère la hiérarchie des comptes et les calculs de soldes
 */
@Service
@Transactional
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    /**
     * Récupère un compte par son numéro
     */
    public Account getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }

    /**
     * Récupère tous les comptes actifs
     */
    public List<Account> getAllActiveAccounts() {
        return accountRepository.findByIsActiveTrueOrderByAccountNumber();
    }

    /**
     * Récupère les comptes par type
     */
    public List<Account> getAccountsByType(AccountType accountType) {
        return accountRepository.findByAccountTypeAndIsActiveTrueOrderByAccountNumber(accountType);
    }

    /**
     * Récupère les comptes racines (sans parent)
     */
    public List<Account> getRootAccounts() {
        return accountRepository.findByParentAccountIsNullAndIsActiveTrueOrderByAccountNumber();
    }

    /**
     * Récupère les sous-comptes d'un compte parent
     */
    public List<Account> getSubAccounts(Long parentAccountId) {
        return accountRepository.findByParentAccountIdAndIsActiveTrueOrderByAccountNumber(parentAccountId);
    }

    /**
     * Crée un nouveau compte
     */
    public Account createAccount(String accountNumber, String accountName,
            AccountType accountType, Account parentAccount) {
        // Vérification de l'unicité du numéro de compte
        if (accountRepository.findByAccountNumber(accountNumber) != null) {
            throw new IllegalArgumentException("Le numéro de compte " + accountNumber + " existe déjà");
        }

        Account account = new Account(accountNumber, accountName, accountType, parentAccount);
        return accountRepository.save(account);
    }

    /**
     * Met à jour un compte existant
     */
    public Account updateAccount(Long accountId, String accountName, String description) {
        Optional<Account> optionalAccount = accountRepository.findById(accountId);
        if (optionalAccount.isEmpty()) {
            throw new IllegalArgumentException("Compte non trouvé avec l'ID: " + accountId);
        }

        Account account = optionalAccount.get();
        account.setAccountName(accountName);
        account.setDescription(description);

        return accountRepository.save(account);
    }

    /**
     * Désactive un compte (soft delete)
     */
    public void deactivateAccount(Long accountId) {
        Optional<Account> optionalAccount = accountRepository.findById(accountId);
        if (optionalAccount.isEmpty()) {
            throw new IllegalArgumentException("Compte non trouvé avec l'ID: " + accountId);
        }

        Account account = optionalAccount.get();

        // Vérifier qu'il n'y a pas de sous-comptes actifs
        List<Account> activeSubAccounts = getSubAccounts(accountId);
        if (!activeSubAccounts.isEmpty()) {
            throw new IllegalStateException("Impossible de désactiver un compte ayant des sous-comptes actifs");
        }

        account.setIsActive(false);
        accountRepository.save(account);
    }

    /**
     * Calcule le solde total d'un type de compte
     */
    public BigDecimal getTotalBalanceByAccountType(AccountType accountType) {
        return accountRepository.getTotalBalanceByAccountType(accountType);
    }

    /**
     * Recherche des comptes par nom (recherche partielle)
     */
    public List<Account> searchAccountsByName(String searchTerm) {
        return accountRepository.findByAccountNameContainingIgnoreCaseAndIsActiveTrue(searchTerm);
    }

    /**
     * Récupère la hiérarchie complète d'un compte (chemin vers la racine)
     */
    public String getAccountHierarchy(Long accountId) {
        Optional<Account> optionalAccount = accountRepository.findById(accountId);
        if (optionalAccount.isEmpty()) {
            return "";
        }

        Account account = optionalAccount.get();
        return account.getFullName();
    }

    /**
     * Valide la cohérence du plan comptable
     * Vérifie que la somme des actifs = somme des passifs + capitaux propres
     */
    public boolean validateChartOfAccountsBalance() {
        BigDecimal totalAssets = getTotalBalanceByAccountType(AccountType.ASSET);
        BigDecimal totalLiabilities = getTotalBalanceByAccountType(AccountType.LIABILITY);
        BigDecimal totalEquity = getTotalBalanceByAccountType(AccountType.EQUITY);

        BigDecimal totalPassive = totalLiabilities.add(totalEquity);

        return totalAssets.compareTo(totalPassive) == 0;
    }

    /**
     * Initialise le plan comptable de base
     */
    @Transactional
    public void initializeBasicChartOfAccounts() {
        // Vérifier si le plan comptable existe déjà
        if (accountRepository.count() > 0) {
            return; // Plan comptable déjà initialisé
        }

        // Classe 1 - Comptes de capitaux
        Account equity = createAccount("1", "COMPTES DE CAPITAUX", AccountType.EQUITY, null);
        createAccount("101", "Capital social", AccountType.EQUITY, equity);
        createAccount("110", "Report à nouveau", AccountType.EQUITY, equity);
        createAccount("120", "Résultat de l'exercice", AccountType.EQUITY, equity);

        // Classe 4 - Comptes de tiers
        Account thirdParties = createAccount("4", "COMPTES DE TIERS", AccountType.ASSET, null);
        Account clients = createAccount("41", "Clients et comptes rattachés", AccountType.ASSET, thirdParties);
        createAccount("411", "Clients", AccountType.ASSET, clients);
        createAccount("416", "Clients douteux", AccountType.ASSET, clients);

        Account suppliers = createAccount("40", "Fournisseurs et comptes rattachés", AccountType.LIABILITY,
                thirdParties);
        createAccount("401", "Fournisseurs", AccountType.LIABILITY, suppliers);

        Account vat = createAccount("44", "État et collectivités publiques", AccountType.LIABILITY, thirdParties);
        createAccount("445", "TVA collectée", AccountType.LIABILITY, vat);
        createAccount("446", "TVA déductible", AccountType.ASSET, vat);

        // Classe 5 - Comptes de trésorerie
        Account treasury = createAccount("5", "COMPTES DE TRESORERIE", AccountType.ASSET, null);
        Account bank = createAccount("51", "Banques", AccountType.ASSET, treasury);
        createAccount("512", "Banques", AccountType.ASSET, bank);
        Account cash = createAccount("53", "Caisse", AccountType.ASSET, treasury);
        createAccount("531", "Caisse", AccountType.ASSET, cash);

        // Classe 6 - Comptes de charges
        Account expenses = createAccount("6", "COMPTES DE CHARGES", AccountType.EXPENSE, null);
        createAccount("601", "Achats de matières premières", AccountType.EXPENSE, expenses);
        createAccount("621", "Personnel extérieur", AccountType.EXPENSE, expenses);
        createAccount("641", "Rémunérations du personnel", AccountType.EXPENSE, expenses);

        // Classe 7 - Comptes de produits
        Account revenues = createAccount("7", "COMPTES DE PRODUITS", AccountType.REVENUE, null);
        Account sales = createAccount("70", "Ventes", AccountType.REVENUE, revenues);
        createAccount("701", "Ventes de services", AccountType.REVENUE, sales);
        createAccount("706", "Prestations de services", AccountType.REVENUE, sales);
    }
}
