package com.freightops.fret.manifeste.dto;

import com.freightops.enums.TransportMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ManifestCreateRequest {

    @NotBlank(message = "Le numéro proforma est obligatoire")
    @Size(max = 100, message = "Le numéro proforma ne doit pas dépasser 100 caractères")
    private String proformaNumber;

    @NotNull(message = "Le mode de transport est obligatoire")
    private TransportMode transportMode;

    @Size(max = 100, message = "Les informations du véhicule ne doivent pas dépasser 100 caractères")
    private String vehicleInfo;

    @Size(max = 200, message = "Le nom du conducteur ne doit pas dépasser 200 caractères")
    private String driverName;

    private LocalDateTime departureDate;

    private LocalDateTime arrivalDate;

    @Valid
    private List<PartyRequest> parties;

    @Valid
    @NotEmpty(message = "Au moins un article de marchandise est requis")
    private List<GoodsItemRequest> goods;

    @Size(max = 1000, message = "Les instructions de livraison ne doivent pas dépasser 1000 caractères")
    private String deliveryInstructions;

    @Size(max = 1000, message = "Les remarques ne doivent pas dépasser 1000 caractères")
    private String remarks;

    @Size(max = 2000, message = "Les pièces jointes ne doivent pas dépasser 2000 caractères")
    private String attachments;

    @Data
    public static class PartyRequest {
        @NotBlank(message = "Le type de partie est obligatoire")
        @Size(max = 20, message = "Le type de partie ne doit pas dépasser 20 caractères")
        private String partyType; // SHIPPER, CONSIGNEE, CLIENT, AGENT

        @NotBlank(message = "Le nom de l'entreprise est obligatoire")
        @Size(max = 200, message = "Le nom de l'entreprise ne doit pas dépasser 200 caractères")
        private String companyName;

        @Size(max = 200, message = "Le nom du contact ne doit pas dépasser 200 caractères")
        private String contactName;

        @Size(max = 500, message = "L'adresse ne doit pas dépasser 500 caractères")
        private String address;

        @Size(max = 100, message = "La ville ne doit pas dépasser 100 caractères")
        private String city;

        @Size(max = 100, message = "Le pays ne doit pas dépasser 100 caractères")
        private String country;

        @Pattern(regexp = "^\\+?[0-9\\s\\-\\(\\)]{0,20}$", message = "Format de téléphone invalide")
        @Size(max = 20, message = "Le téléphone ne doit pas dépasser 20 caractères")
        private String phone;

        @Size(max = 100, message = "L'email ne doit pas dépasser 100 caractères")
        private String email;

        @Size(max = 50, message = "L'identifiant fiscal ne doit pas dépasser 50 caractères")
        private String taxId;
    }

    @Data
    public static class GoodsItemRequest {
        @Size(max = 50, message = "Le numéro de suivi ne doit pas dépasser 50 caractères")
        private String trackingNumber;

        @NotBlank(message = "La description est obligatoire")
        @Size(max = 500, message = "La description ne doit pas dépasser 500 caractères")
        private String description;

        @Size(max = 100, message = "L'emballage ne doit pas dépasser 100 caractères")
        private String packaging;

        @NotNull(message = "Le nombre de colis est obligatoire")
        @Min(value = 1, message = "Le nombre de colis doit être au moins 1")
        private Integer packageCount;

        @NotNull(message = "Le poids est obligatoire")
        @DecimalMin(value = "0.0", inclusive = false, message = "Le poids doit être supérieur à 0")
        private BigDecimal weight;

        @NotNull(message = "Le volume est obligatoire")
        @DecimalMin(value = "0.0", inclusive = false, message = "Le volume doit être supérieur à 0")
        private BigDecimal volume;

        @NotNull(message = "La valeur est obligatoire")
        @DecimalMin(value = "0.0", message = "La valeur doit être non négative")
        private BigDecimal value;

        @Size(max = 10, message = "La devise ne doit pas dépasser 10 caractères")
        private String currency = "XAF";

        @Size(max = 100, message = "L'origine ne doit pas dépasser 100 caractères")
        private String origin;

        @Size(max = 100, message = "La destination ne doit pas dépasser 100 caractères")
        private String destination;

        @Size(max = 500, message = "Les instructions spéciales ne doivent pas dépasser 500 caractères")
        private String specialInstructions;

        @Size(max = 100, message = "Le code de manutention ne doit pas dépasser 100 caractères")
        private String handlingCode;
    }
}
