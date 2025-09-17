-- Migration pour créer la table des paiements LTA
-- V1.5 - Création table lta_payments pour encaissement LTA avec comptabilité double écriture

CREATE TABLE lta_payments (
    id BIGSERIAL PRIMARY KEY,
    lta_id BIGINT NOT NULL,
    amount DECIMAL(12,2) NOT NULL CHECK (amount > 0),
    payment_date DATE NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    reference VARCHAR(100),
    notes VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    cash_box_id BIGINT,
    debit_account VARCHAR(20),
    credit_account VARCHAR(20),
    accounting_reference VARCHAR(50) UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Contraintes de clés étrangères
    CONSTRAINT fk_lta_payments_lta FOREIGN KEY (lta_id) REFERENCES ltas(id) ON DELETE CASCADE,
    CONSTRAINT fk_lta_payments_cash_box FOREIGN KEY (cash_box_id) REFERENCES cash_boxes(id) ON DELETE SET NULL,
    
    -- Contraintes de validation
    CONSTRAINT chk_payment_method CHECK (payment_method IN ('ESPECES', 'PORT_DU', 'CHEQUE', 'VIREMENT', 'MOBILE_MONEY')),
    CONSTRAINT chk_payment_status CHECK (status IN ('PENDING', 'COMPLETED', 'CANCELLED', 'FAILED'))
);

-- Index pour améliorer les performances
CREATE INDEX idx_lta_payments_lta_id ON lta_payments(lta_id);
CREATE INDEX idx_lta_payments_payment_date ON lta_payments(payment_date);
CREATE INDEX idx_lta_payments_status ON lta_payments(status);
CREATE INDEX idx_lta_payments_payment_method ON lta_payments(payment_method);
CREATE INDEX idx_lta_payments_accounting_ref ON lta_payments(accounting_reference);

-- Trigger pour mettre à jour updated_at automatiquement
CREATE OR REPLACE FUNCTION update_lta_payments_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_lta_payments_updated_at
    BEFORE UPDATE ON lta_payments
    FOR EACH ROW
    EXECUTE FUNCTION update_lta_payments_updated_at();

-- Commentaires pour documentation
COMMENT ON TABLE lta_payments IS 'Table des paiements pour les LTA avec comptabilité double écriture';
COMMENT ON COLUMN lta_payments.lta_id IS 'Référence vers la LTA payée';
COMMENT ON COLUMN lta_payments.amount IS 'Montant du paiement en devise de la LTA';
COMMENT ON COLUMN lta_payments.payment_method IS 'Méthode de paiement: ESPECES, PORT_DU, etc.';
COMMENT ON COLUMN lta_payments.debit_account IS 'Compte comptable débité (ex: 5111 - Caisse)';
COMMENT ON COLUMN lta_payments.credit_account IS 'Compte comptable crédité (ex: 7061 - Ventes transport)';
COMMENT ON COLUMN lta_payments.accounting_reference IS 'Référence comptable unique pour traçabilité';
