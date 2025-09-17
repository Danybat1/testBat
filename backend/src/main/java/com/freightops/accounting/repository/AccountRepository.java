package com.freightops.accounting.repository;

import com.freightops.accounting.entity.Account;
import com.freightops.accounting.enums.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour la gestion des comptes comptables
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Trouve un compte par son numéro
     */
    Account findByAccountNumber(String accountNumber);

    /**
     * Trouve tous les comptes actifs triés par numéro
     */
    List<Account> findByIsActiveTrueOrderByAccountNumber();

    /**
     * Trouve les comptes par type et actifs
     */
    List<Account> findByAccountTypeAndIsActiveTrueOrderByAccountNumber(AccountType accountType);

    /**
     * Trouve les comptes racines (sans parent) actifs
     */
    List<Account> findByParentAccountIsNullAndIsActiveTrueOrderByAccountNumber();

    /**
     * Trouve les sous-comptes d'un compte parent
     */
    List<Account> findByParentAccountIdAndIsActiveTrueOrderByAccountNumber(Long parentAccountId);

    /**
     * Recherche des comptes par nom (recherche partielle)
     */
    List<Account> findByAccountNameContainingIgnoreCaseAndIsActiveTrue(String searchTerm);

    /**
     * Compte le nombre total de comptes
     */
    long count();

    /**
     * Trouve les comptes de détail (sans sous-comptes) par type
     */
    @Query("SELECT a FROM Account a WHERE a.accountType = :accountType AND a.isActive = true AND SIZE(a.subAccounts) = 0 ORDER BY a.accountNumber")
    List<Account> findDetailAccountsByType(@Param("accountType") AccountType accountType);

    /**
     * Calcule le solde total par type de compte
     */
    @Query("SELECT COALESCE(SUM(a.balance), 0) FROM Account a WHERE a.accountType = :accountType AND a.isActive = true AND SIZE(a.subAccounts) = 0")
    java.math.BigDecimal getTotalBalanceByAccountType(@Param("accountType") AccountType accountType);
}
