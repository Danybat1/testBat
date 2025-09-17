package com.freightops.repository;

import com.freightops.entity.CashBox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CashBoxRepository extends JpaRepository<CashBox, Long> {

    List<CashBox> findByActiveTrue();

    CashBox findByNameIgnoreCase(String name);

    @Query("SELECT c FROM CashBox c WHERE c.active = true ORDER BY c.name")
    List<CashBox> findActiveCashBoxesOrderByName();

    @Query("SELECT SUM(c.currentBalance) FROM CashBox c WHERE c.active = true")
    BigDecimal sumCurrentBalanceOfActiveCashBoxes();

    boolean existsByNameIgnoreCase(String name);

    @Query("SELECT c FROM CashBox c WHERE c.currentBalance < :threshold AND c.active = true")
    List<CashBox> findCashBoxesWithLowBalance(@Param("threshold") BigDecimal threshold);

    // Method for cash statement - get first active cash box
    Optional<CashBox> findFirstByActiveTrue();
}
