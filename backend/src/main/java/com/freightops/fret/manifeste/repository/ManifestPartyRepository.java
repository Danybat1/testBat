package com.freightops.fret.manifeste.repository;

import com.freightops.entity.ManifestParty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ManifestPartyRepository extends JpaRepository<ManifestParty, Long> {

    List<ManifestParty> findByManifestId(Long manifestId);

    List<ManifestParty> findByPartyType(String partyType);

    Optional<ManifestParty> findByManifestIdAndPartyType(Long manifestId, String partyType);

    List<ManifestParty> findByCompanyNameContainingIgnoreCase(String companyName);

    @Query("SELECT p FROM ManifestParty p WHERE p.manifest.id = :manifestId AND p.partyType = :partyType")
    Optional<ManifestParty> findPartyByManifestAndType(@Param("manifestId") Long manifestId,
            @Param("partyType") String partyType);

    @Query("SELECT p FROM ManifestParty p WHERE p.companyName LIKE %:searchTerm% OR p.contactName LIKE %:searchTerm%")
    List<ManifestParty> searchParties(@Param("searchTerm") String searchTerm);

    @Query("SELECT DISTINCT p.companyName FROM ManifestParty p WHERE p.partyType = :partyType ORDER BY p.companyName")
    List<String> findDistinctCompanyNamesByPartyType(@Param("partyType") String partyType);

    void deleteByManifestId(Long manifestId);
}
