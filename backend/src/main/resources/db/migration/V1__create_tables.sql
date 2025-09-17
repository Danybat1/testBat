-- FreightOps Database Schema Migration
-- Version 1: Create initial tables for LTA, Client, and Invoice

-- Create Client table
CREATE TABLE client (
    id BIGSERIAL PRIMARY KEY,
    client_code VARCHAR(20) UNIQUE NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    contact_person VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(20),
    address VARCHAR(500),
    city VARCHAR(100),
    state VARCHAR(100),
    zip_code VARCHAR(20),
    country VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Create indexes for Client table
CREATE INDEX idx_client_code ON client(client_code);
CREATE INDEX idx_client_email ON client(email);
CREATE INDEX idx_client_status ON client(status);

-- Create LTA table
CREATE TABLE lta (
    id BIGSERIAL PRIMARY KEY,
    lta_number VARCHAR(50) UNIQUE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    shipper VARCHAR(255) NOT NULL,
    consignee VARCHAR(255) NOT NULL,
    weight DECIMAL(10,2) NOT NULL CHECK (weight > 0),
    tracking_number VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(500),
    origin VARCHAR(255),
    destination VARCHAR(255),
    declared_value DECIMAL(12,2),
    invoice_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Create indexes for LTA table
CREATE INDEX idx_lta_number ON lta(lta_number);
CREATE INDEX idx_tracking_number ON lta(tracking_number);
CREATE INDEX idx_lta_status ON lta(status);
CREATE INDEX idx_lta_shipper ON lta(shipper);
CREATE INDEX idx_lta_consignee ON lta(consignee);
CREATE INDEX idx_lta_created_at ON lta(created_at);

-- Create Invoice table
CREATE TABLE invoice (
    id BIGSERIAL PRIMARY KEY,
    invoice_number VARCHAR(50) UNIQUE NOT NULL,
    invoice_date DATE NOT NULL,
    due_date DATE,
    client_id BIGINT NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL CHECK (subtotal > 0),
    tax_amount DECIMAL(12,2) DEFAULT 0,
    discount_amount DECIMAL(12,2) DEFAULT 0,
    total_amount DECIMAL(12,2) NOT NULL CHECK (total_amount > 0),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_invoice_client FOREIGN KEY (client_id) REFERENCES client(id) ON DELETE RESTRICT
);

-- Create indexes for Invoice table
CREATE INDEX idx_invoice_number ON invoice(invoice_number);
CREATE INDEX idx_invoice_status ON invoice(status);
CREATE INDEX idx_invoice_date ON invoice(invoice_date);
CREATE INDEX idx_invoice_client_id ON invoice(client_id);
CREATE INDEX idx_invoice_due_date ON invoice(due_date);

-- Add foreign key constraint for LTA to Invoice
ALTER TABLE lta ADD CONSTRAINT fk_lta_invoice 
    FOREIGN KEY (invoice_id) REFERENCES invoice(id) ON DELETE SET NULL;

-- Create tracking table for LTA status history
CREATE TABLE lta_tracking (
    id BIGSERIAL PRIMARY KEY,
    lta_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    location VARCHAR(255),
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    CONSTRAINT fk_tracking_lta FOREIGN KEY (lta_id) REFERENCES lta(id) ON DELETE CASCADE
);

-- Create indexes for tracking table
CREATE INDEX idx_tracking_lta_id ON lta_tracking(lta_id);
CREATE INDEX idx_tracking_status ON lta_tracking(status);
CREATE INDEX idx_tracking_created_at ON lta_tracking(created_at);

-- Create audit trigger function for updated_at timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers for updated_at columns
CREATE TRIGGER update_client_updated_at 
    BEFORE UPDATE ON client 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_lta_updated_at 
    BEFORE UPDATE ON lta 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_invoice_updated_at 
    BEFORE UPDATE ON invoice 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert sample data
INSERT INTO client (client_code, company_name, contact_person, email, phone, address, city, state, zip_code, country, status) VALUES
('CLI001', 'Acme Corporation', 'John Doe', 'john.doe@acme.com', '+1-555-0101', '123 Main St', 'New York', 'NY', '10001', 'USA', 'ACTIVE'),
('CLI002', 'Global Logistics Inc', 'Jane Smith', 'jane.smith@globallogistics.com', '+1-555-0102', '456 Oak Ave', 'Los Angeles', 'CA', '90001', 'USA', 'ACTIVE'),
('CLI003', 'Express Freight Co', 'Bob Johnson', 'bob.johnson@expressfreight.com', '+1-555-0103', '789 Pine Rd', 'Chicago', 'IL', '60601', 'USA', 'ACTIVE');

-- Insert sample LTA data
INSERT INTO lta (lta_number, status, shipper, consignee, weight, tracking_number, description, origin, destination, declared_value) VALUES
('LTA-20240001', 'IN_TRANSIT', 'Acme Corporation', 'Global Logistics Inc', 1250.50, 'TRK-20240001', 'Electronics shipment', 'New York, NY', 'Los Angeles, CA', 15000.00),
('LTA-20240002', 'DELIVERED', 'Global Logistics Inc', 'Express Freight Co', 850.25, 'TRK-20240002', 'Automotive parts', 'Los Angeles, CA', 'Chicago, IL', 8500.00),
('LTA-20240003', 'PENDING', 'Express Freight Co', 'Acme Corporation', 2100.75, 'TRK-20240003', 'Industrial equipment', 'Chicago, IL', 'New York, NY', 25000.00);

-- Insert sample tracking data
INSERT INTO lta_tracking (lta_id, status, location, notes, created_by) VALUES
(1, 'PENDING', 'New York, NY', 'Shipment created', 'admin'),
(1, 'CONFIRMED', 'New York, NY', 'Shipment confirmed and ready for pickup', 'agent'),
(1, 'IN_TRANSIT', 'Philadelphia, PA', 'In transit to destination', 'system'),
(2, 'PENDING', 'Los Angeles, CA', 'Shipment created', 'admin'),
(2, 'CONFIRMED', 'Los Angeles, CA', 'Shipment confirmed', 'agent'),
(2, 'IN_TRANSIT', 'Phoenix, AZ', 'In transit', 'system'),
(2, 'OUT_FOR_DELIVERY', 'Chicago, IL', 'Out for delivery', 'system'),
(2, 'DELIVERED', 'Chicago, IL', 'Successfully delivered', 'system'),
(3, 'PENDING', 'Chicago, IL', 'Shipment created', 'admin');

-- Create views for reporting
CREATE VIEW lta_summary AS
SELECT 
    l.id,
    l.lta_number,
    l.tracking_number,
    l.status,
    l.shipper,
    l.consignee,
    l.weight,
    l.declared_value,
    l.origin,
    l.destination,
    l.created_at,
    l.updated_at,
    CASE 
        WHEN l.status = 'DELIVERED' THEN 'Completed'
        WHEN l.status IN ('CANCELLED', 'RETURNED') THEN 'Closed'
        ELSE 'Active'
    END as category
FROM lta l;

CREATE VIEW client_summary AS
SELECT 
    c.id,
    c.client_code,
    c.company_name,
    c.status,
    COUNT(l.id) as total_shipments,
    COALESCE(SUM(l.declared_value), 0) as total_value,
    c.created_at
FROM client c
LEFT JOIN lta l ON l.shipper = c.company_name OR l.consignee = c.company_name
GROUP BY c.id, c.client_code, c.company_name, c.status, c.created_at;

-- Grant permissions (adjust as needed for your environment)
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO freightops_user;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO freightops_user;
