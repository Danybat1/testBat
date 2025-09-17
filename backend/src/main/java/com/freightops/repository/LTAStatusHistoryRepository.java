package com.freightops.repository;

import com.freightops.entity.LTAStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for LTA Status History operations
 */
@Repository
public interface LTAStatusHistoryRepository extends JpaRepository<LTAStatusHistory, Long> {

    /**
     * Find all status history for a specific LTA, ordered by change date
     */
    @Query("SELECT h FROM LTAStatusHistory h WHERE h.lta.id = :ltaId ORDER BY h.changedAt ASC")
    List<LTAStatusHistory> findByLtaIdOrderByChangedAtAsc(@Param("ltaId") Long ltaId);

    /**
     * Find all status history for a specific LTA by tracking number
     */
    @Query("SELECT h FROM LTAStatusHistory h WHERE h.lta.trackingNumber = :trackingNumber ORDER BY h.changedAt ASC")
    List<LTAStatusHistory> findByLtaTrackingNumberOrderByChangedAtAsc(@Param("trackingNumber") String trackingNumber);

    /**
     * Find the latest status change for a specific LTA
     */
    @Query("SELECT h FROM LTAStatusHistory h WHERE h.lta.id = :ltaId ORDER BY h.changedAt DESC LIMIT 1")
    LTAStatusHistory findLatestByLtaId(@Param("ltaId") Long ltaId);
}
