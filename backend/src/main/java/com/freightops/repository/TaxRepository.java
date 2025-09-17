package com.freightops.repository;

import com.freightops.entity.Tax;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaxRepository extends JpaRepository<Tax, Long> {
    
    List<Tax> findByActiveTrue();
    
    Tax findByNameIgnoreCase(String name);
    
    @Query("SELECT t FROM Tax t WHERE t.active = true ORDER BY t.name")
    List<Tax> findActiveTaxesOrderByName();
    
    boolean existsByNameIgnoreCase(String name);
}
