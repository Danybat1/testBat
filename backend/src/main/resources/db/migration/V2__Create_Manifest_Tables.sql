-- FreightOps Database Schema Migration
-- Version 2: Create Manifest tables

-- Create Manifest table
CREATE TABLE manifests (
    id BIGSERIAL PRIMARY KEY,
    manifest_number VARCHAR(50) UNIQUE,
    tracking_number VARCHAR(50) UNIQUE,
    qr_code_data VARCHAR(500),
    
    -- General Information
    proforma_number VARCHAR(100) NOT NULL,
    transport_mode VARCHAR(20) NOT NULL,
    vehicle_info VARCHAR(100),
    driver_name VARCHAR(200),
    departure_date TIMESTAMP,
    arrival_date TIMESTAMP,
    
    -- Status and tracking
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    
    -- Totals
    total_weight DECIMAL(10,3) DEFAULT 0.0,
    total_volume DECIMAL(10,3) DEFAULT 0.0,
    total_packages INTEGER DEFAULT 0,
    total_value DECIMAL(12,2) DEFAULT 0.0,
    
    -- Instructions and remarks
    delivery_instructions VARCHAR(1000),
    remarks VARCHAR(1000),
    
    -- Signatures
    loading_signature_date TIMESTAMP,
    loading_signature_remarks VARCHAR(500),
    delivery_signature_date TIMESTAMP,
    delivery_signature_remarks VARCHAR(500),
    
    -- Attachments
    attachments VARCHAR(2000),
    
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create ManifestParty table
CREATE TABLE manifest_parties (
    id BIGSERIAL PRIMARY KEY,
    manifest_id BIGINT NOT NULL,
    party_type VARCHAR(20) NOT NULL, -- SHIPPER, CONSIGNEE, CLIENT, AGENT
    company_name VARCHAR(200) NOT NULL,
    contact_name VARCHAR(200),
    address VARCHAR(500),
    city VARCHAR(100),
    country VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    tax_id VARCHAR(50),
    
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraint
    CONSTRAINT fk_manifest_party_manifest FOREIGN KEY (manifest_id) REFERENCES manifests(id) ON DELETE CASCADE
);

-- Create ManifestGoods table
CREATE TABLE manifest_goods (
    id BIGSERIAL PRIMARY KEY,
    manifest_id BIGINT NOT NULL,
    line_number INTEGER NOT NULL,
    tracking_number VARCHAR(50),
    description VARCHAR(500) NOT NULL,
    packaging VARCHAR(100),
    package_count INTEGER NOT NULL,
    weight DECIMAL(10,3) NOT NULL,
    volume DECIMAL(10,3) NOT NULL,
    value DECIMAL(12,2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'XAF',
    origin VARCHAR(100),
    destination VARCHAR(100),
    special_instructions VARCHAR(500),
    handling_code VARCHAR(100),
    
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraint
    CONSTRAINT fk_manifest_goods_manifest FOREIGN KEY (manifest_id) REFERENCES manifests(id) ON DELETE CASCADE
);

-- Create indexes for Manifest table
CREATE INDEX idx_manifest_number ON manifests(manifest_number);
CREATE INDEX idx_manifest_tracking_number ON manifests(tracking_number);
CREATE INDEX idx_manifest_status ON manifests(status);
CREATE INDEX idx_manifest_proforma_number ON manifests(proforma_number);
CREATE INDEX idx_manifest_transport_mode ON manifests(transport_mode);
CREATE INDEX idx_manifest_created_at ON manifests(created_at);
CREATE INDEX idx_manifest_departure_date ON manifests(departure_date);

-- Create indexes for ManifestParty table
CREATE INDEX idx_manifest_party_manifest_id ON manifest_parties(manifest_id);
CREATE INDEX idx_manifest_party_type ON manifest_parties(party_type);
CREATE INDEX idx_manifest_party_company_name ON manifest_parties(company_name);
CREATE INDEX idx_manifest_party_manifest_type ON manifest_parties(manifest_id, party_type);

-- Create indexes for ManifestGoods table
CREATE INDEX idx_manifest_goods_manifest_id ON manifest_goods(manifest_id);
CREATE INDEX idx_manifest_goods_line_number ON manifest_goods(manifest_id, line_number);
CREATE INDEX idx_manifest_goods_tracking_number ON manifest_goods(tracking_number);
CREATE INDEX idx_manifest_goods_description ON manifest_goods(description);

-- Create unique constraint for manifest party types (one party per type per manifest)
CREATE UNIQUE INDEX idx_unique_manifest_party_type ON manifest_parties(manifest_id, party_type);

-- Add check constraints
ALTER TABLE manifests ADD CONSTRAINT chk_manifest_status 
    CHECK (status IN ('DRAFT', 'CONFIRMED', 'IN_TRANSIT', 'DELIVERED', 'CANCELLED'));

ALTER TABLE manifests ADD CONSTRAINT chk_manifest_transport_mode 
    CHECK (transport_mode IN ('AIR', 'ROAD', 'SEA', 'RAIL', 'MULTIMODAL'));

ALTER TABLE manifest_parties ADD CONSTRAINT chk_party_type 
    CHECK (party_type IN ('SHIPPER', 'CONSIGNEE', 'CLIENT', 'AGENT'));

ALTER TABLE manifests ADD CONSTRAINT chk_total_weight_positive 
    CHECK (total_weight >= 0);

ALTER TABLE manifests ADD CONSTRAINT chk_total_volume_positive 
    CHECK (total_volume >= 0);

ALTER TABLE manifests ADD CONSTRAINT chk_total_packages_positive 
    CHECK (total_packages >= 0);

ALTER TABLE manifests ADD CONSTRAINT chk_total_value_positive 
    CHECK (total_value >= 0);

ALTER TABLE manifest_goods ADD CONSTRAINT chk_goods_weight_positive 
    CHECK (weight > 0);

ALTER TABLE manifest_goods ADD CONSTRAINT chk_goods_volume_positive 
    CHECK (volume > 0);

ALTER TABLE manifest_goods ADD CONSTRAINT chk_goods_value_positive 
    CHECK (value >= 0);

ALTER TABLE manifest_goods ADD CONSTRAINT chk_goods_package_count_positive 
    CHECK (package_count > 0);

ALTER TABLE manifest_goods ADD CONSTRAINT chk_goods_line_number_positive 
    CHECK (line_number > 0);
