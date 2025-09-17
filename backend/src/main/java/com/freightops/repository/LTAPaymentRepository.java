package com.freightops.repository;

import com.freightops.entity.LTA;
import com.freightops.entity.LTAPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository pour la gestion des paiements LTA
 */
@Repository
public interface LTAPaymentRepository extends JpaRepository<LTAPayment, Long> {

    /**
     * Trouve tous les paiements pour une LTA donnée
     */
    List<LTAPayment> findByLtaId(Long ltaId);

    /**
     * Trouve tous les paiements pour une LTA donnée, triés par date de paiement
     * décroissante
     */
    List<LTAPayment> findByLtaIdOrderByPaymentDateDesc(Long ltaId);

    /**
     * Calcule le montant total payé pour une LTA
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM LTAPayment p WHERE p.lta.id = :ltaId")
    BigDecimal getTotalPaidAmountForLTA(@Param("ltaId") Long ltaId);

    /**
     * Trouve les LTA éligibles pour paiement
     */
    @Query("""
            SELECT l FROM LTA l
            WHERE l.calculatedCost IS NOT NULL
            AND l.calculatedCost > 0
            AND l.status IN ('CONFIRMED', 'IN_TRANSIT', 'DELIVERED')
            AND l.paymentMode IN ('CASH', 'PORT_DU')
            ORDER BY l.createdAt DESC
            """)
    List<LTA> findLTAsEligibleForPayment();

    /**
     * Trouve les LTA non payées
     */
    @Query("""
            SELECT l FROM LTA l
            WHERE l.calculatedCost IS NOT NULL
            AND l.calculatedCost > 0
            AND l.status IN ('CONFIRMED', 'IN_TRANSIT', 'DELIVERED')
            AND l.paymentMode IN ('CASH', 'PORT_DU')
            AND (SELECT COALESCE(SUM(p.amount), 0) FROM LTAPayment p WHERE p.lta.id = l.id) < l.calculatedCost
            ORDER BY l.createdAt DESC
            """)
    List<LTA> findUnpaidLTAs();
}
