package com.freightops.accounting.enums;

/**
 * Types de comptes selon le plan comptable OHADA
 * Classe 1: Comptes de ressources durables (EQUITY, LIABILITY)
 * Classe 2: Comptes d'actif immobilisé (ASSET)
 * Classe 3: Comptes de stocks (ASSET)
 * Classe 4: Comptes de tiers (ASSET, LIABILITY)
 * Classe 5: Comptes de trésorerie (ASSET)
 * Classe 6: Comptes de charges (EXPENSE)
 * Classe 7: Comptes de produits (REVENUE)
 */
public enum AccountType {
    ASSET("Actif", "Biens et créances de l'entreprise"),
    LIABILITY("Passif", "Dettes et obligations de l'entreprise"),
    EQUITY("Capitaux propres", "Capitaux appartenant aux propriétaires"),
    REVENUE("Produits", "Revenus et gains de l'entreprise"),
    EXPENSE("Charges", "Coûts et dépenses de l'entreprise");

    private final String label;
    private final String description;

    AccountType(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Détermine si le type de compte augmente au débit
     * 
     * @return true si le compte augmente au débit, false s'il augmente au crédit
     */
    public boolean increasesWithDebit() {
        return this == ASSET || this == EXPENSE;
    }

    /**
     * Détermine si le type de compte augmente au crédit
     * 
     * @return true si le compte augmente au crédit, false s'il augmente au débit
     */
    public boolean increasesWithCredit() {
        return this == LIABILITY || this == EQUITY || this == REVENUE;
    }
}
