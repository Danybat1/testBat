package com.freightops.fret.manifeste.repository;

import com.freightops.fret.manifeste.model.ManifestItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ManifestItemRepository extends JpaRepository<ManifestItem, Long> {

    List<ManifestItem> findByManifestId(Long manifestId);

    List<ManifestItem> findByManifestIdOrderByLineNumber(Long manifestId);

    List<ManifestItem> findByTrackingNumber(String trackingNumber);

    @Query("SELECT mi FROM ManifestItem mi WHERE mi.manifest.id = :manifestId AND mi.trackingNumber LIKE %:trackingNumber%")
    List<ManifestItem> findByManifestIdAndTrackingNumberContaining(@Param("manifestId") Long manifestId,
            @Param("trackingNumber") String trackingNumber);

    @Query("SELECT mi FROM ManifestItem mi WHERE mi.manifest.id = :manifestId AND mi.description LIKE %:description%")
    List<ManifestItem> findByManifestIdAndDescriptionContaining(@Param("manifestId") Long manifestId,
            @Param("description") String description);

    void deleteByManifestId(Long manifestId);
}
