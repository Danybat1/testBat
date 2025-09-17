package com.freightops.fret.manifeste.repository;

import com.freightops.fret.manifeste.model.FreightManifest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FreightManifestRepository extends JpaRepository<FreightManifest, Long> {

        Optional<FreightManifest> findByManifestNumber(String manifestNumber);

        List<FreightManifest> findByStatus(String status);

        Long countByStatus(String status);

        @Query("SELECT fm FROM FreightManifest fm ORDER BY fm.createdAt DESC")
        List<FreightManifest> findAllOrderByCreatedAtDesc();

        @Query("SELECT fm FROM FreightManifest fm WHERE fm.shipperName LIKE %:shipper% OR fm.consigneeName LIKE %:consignee%")
        List<FreightManifest> findByShipperOrConsignee(@Param("shipper") String shipper,
                        @Param("consignee") String consignee);

        List<FreightManifest> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}
