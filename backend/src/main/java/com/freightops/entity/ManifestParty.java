package com.freightops.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * ManifestParty entity representing parties involved in a manifest
 * (shipper, consignee, client, agent)
 */
@Entity
@Table(name = "manifest_parties")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class ManifestParty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Manifest is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manifest_id", nullable = false)
    private Manifest manifest;

    @NotBlank(message = "Party type is required")
    @Size(max = 20, message = "Party type must not exceed 20 characters")
    @Column(name = "party_type", nullable = false, length = 20)
    private String partyType; // SHIPPER, CONSIGNEE, CLIENT, AGENT

    @NotBlank(message = "Company name is required")
    @Size(max = 200, message = "Company name must not exceed 200 characters")
    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;

    @Size(max = 200, message = "Contact name must not exceed 200 characters")
    @Column(name = "contact_name", length = 200)
    private String contactName;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    @Column(name = "address", length = 500)
    private String address;

    @Size(max = 100, message = "City must not exceed 100 characters")
    @Column(name = "city", length = 100)
    private String city;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    @Column(name = "country", length = 100)
    private String country;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    @Column(name = "phone", length = 20)
    private String phone;

    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Column(name = "email", length = 100)
    private String email;

    @Size(max = 50, message = "Tax ID must not exceed 50 characters")
    @Column(name = "tax_id", length = 50)
    private String taxId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors for convenience
    public ManifestParty(Manifest manifest, String partyType, String companyName) {
        this.manifest = manifest;
        this.partyType = partyType;
        this.companyName = companyName;
    }

    public ManifestParty(String partyType, String companyName, String contactName,
            String address, String city, String country, String phone, String email) {
        this.partyType = partyType;
        this.companyName = companyName;
        this.contactName = contactName;
        this.address = address;
        this.city = city;
        this.country = country;
        this.phone = phone;
        this.email = email;
    }

    @Override
    public String toString() {
        return partyType + ": " + companyName;
    }
}
