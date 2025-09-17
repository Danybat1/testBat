package com.freightops.fret.manifeste.repository;

import com.freightops.entity.ManifestGoods;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ManifestGoodsRepository extends JpaRepository<ManifestGoods, Long> {

    List<ManifestGoods> findByManifestId(Long manifestId);

    List<ManifestGoods> findByManifestIdOrderByLineNumber(Long manifestId);

    List<ManifestGoods> findByTrackingNumber(String trackingNumber);

    List<ManifestGoods> findByDescriptionContainingIgnoreCase(String description);

    @Query("SELECT g FROM ManifestGoods g WHERE g.manifest.id = :manifestId ORDER BY g.lineNumber")
    List<ManifestGoods> findGoodsByManifestOrderedByLine(@Param("manifestId") Long manifestId);

    @Query("SELECT SUM(g.weight) FROM ManifestGoods g WHERE g.manifest.id = :manifestId")
    BigDecimal getTotalWeightByManifest(@Param("manifestId") Long manifestId);

    @Query("SELECT SUM(g.volume) FROM ManifestGoods g WHERE g.manifest.id = :manifestId")
    BigDecimal getTotalVolumeByManifest(@Param("manifestId") Long manifestId);

    @Query("SELECT SUM(g.value) FROM ManifestGoods g WHERE g.manifest.id = :manifestId")
    BigDecimal getTotalValueByManifest(@Param("manifestId") Long manifestId);

    @Query("SELECT SUM(g.packageCount) FROM ManifestGoods g WHERE g.manifest.id = :manifestId")
    Integer getTotalPackagesByManifest(@Param("manifestId") Long manifestId);

    @Query("SELECT g FROM ManifestGoods g WHERE g.description LIKE %:searchTerm% OR g.packaging LIKE %:searchTerm%")
    List<ManifestGoods> searchGoods(@Param("searchTerm") String searchTerm);

    @Query("SELECT MAX(g.lineNumber) FROM ManifestGoods g WHERE g.manifest.id = :manifestId")
    Integer getMaxLineNumberByManifest(@Param("manifestId") Long manifestId);

    void deleteByManifestId(Long manifestId);
}
