package com.freightops.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for LTA Status History responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LTAStatusHistoryResponse {

    private Long id;
    private String previousStatus;
    private String newStatus;
    private String changedBy;
    private String changeReason;
    private LocalDateTime changedAt;
    private String statusLabel;
    private String statusDescription;

    public LTAStatusHistoryResponse(String previousStatus, String newStatus, String changedBy,
            LocalDateTime changedAt) {
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changedBy = changedBy;
        this.changedAt = changedAt;
        this.statusLabel = getStatusLabel(newStatus);
        this.statusDescription = getStatusDescription(newStatus);
    }

    private String getStatusLabel(String status) {
        if (status == null)
            return "Inconnu";
        return switch (status) {
            case "DRAFT" -> "Brouillon";
            case "CONFIRMED" -> "Confirmé";
            case "IN_TRANSIT" -> "En transit";
            case "DELIVERED" -> "Livré";
            case "CANCELLED" -> "Annulé";
            default -> status;
        };
    }

    private String getStatusDescription(String status) {
        if (status == null)
            return "Statut inconnu";
        return switch (status) {
            case "DRAFT" -> "LTA créée en brouillon";
            case "CONFIRMED" -> "LTA confirmée et prête pour expédition";
            case "IN_TRANSIT" -> "Colis en cours de transport";
            case "DELIVERED" -> "Colis livré à destination";
            case "CANCELLED" -> "LTA annulée";
            default -> "Changement de statut vers " + status;
        };
    }
}
