package com.freightops.repository;

import com.freightops.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    
    List<BankAccount> findByActiveTrue();
    
    Optional<BankAccount> findByAccountNumber(String accountNumber);
    
    BankAccount findByAccountNameIgnoreCase(String accountName);
    
    List<BankAccount> findByBankNameIgnoreCase(String bankName);
    
    @Query("SELECT b FROM BankAccount b WHERE b.active = true ORDER BY b.accountName")
    List<BankAccount> findActiveBankAccountsOrderByAccountName();
    
    @Query("SELECT SUM(b.currentBalance) FROM BankAccount b WHERE b.active = true")
    BigDecimal sumCurrentBalanceOfActiveBankAccounts();
    
    boolean existsByAccountNumber(String accountNumber);
    
    boolean existsByAccountNameIgnoreCase(String accountName);
    
    @Query("SELECT b FROM BankAccount b WHERE b.currentBalance < :threshold AND b.active = true")
    List<BankAccount> findBankAccountsWithLowBalance(@Param("threshold") BigDecimal threshold);
}
