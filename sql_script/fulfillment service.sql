CREATE DATABASE IF NOT EXISTS fulfillment_service 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE fulfillment_service;

CREATE TABLE payments (
    id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    transaction_id VARCHAR(100) NOT NULL UNIQUE,
    external_transaction_id VARCHAR(100) NULL,
    payment_method ENUM('cod', 'bank_transfer', 'vnpay', 'momo', 'zalopay') NOT NULL,
    payment_gateway VARCHAR(50) NULL,
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'VND',
    status ENUM('pending', 'processing', 'success', 'failed', 'refunded') NOT NULL DEFAULT 'pending',
    paid_at DATETIME NULL,
    callback_url VARCHAR(500) NULL,
    return_url VARCHAR(500) NULL,
    error_code VARCHAR(50) NULL,
    error_message TEXT NULL,
    deleted_at DATETIME NULL,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    last_modified_by VARCHAR(36),
    
    INDEX idx_order_id (order_id),
    INDEX idx_transaction_id (transaction_id),
    INDEX idx_external_transaction_id (external_transaction_id),
    INDEX idx_payment_method (payment_method),
    INDEX idx_status (status),
    INDEX idx_paid_at (paid_at),
    INDEX idx_created_date (created_date),
    INDEX idx_deleted_at (deleted_at),
    
    CONSTRAINT chk_amount CHECK (amount > 0),
    CONSTRAINT chk_paid_at CHECK (
        (status = 'success' AND paid_at IS NOT NULL) OR
        (status != 'success')
    )
) ENGINE=InnoDB;

CREATE TABLE payment_audit_logs (
    id VARCHAR(36) PRIMARY KEY,
    payment_id VARCHAR(36) NOT NULL,
    request_payload JSON NOT NULL,
    response_payload JSON NULL,
    status_code INT NULL,
    deleted_at DATETIME NULL,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    last_modified_by VARCHAR(36),
    
    INDEX idx_payment_id (payment_id),
    INDEX idx_status_code (status_code),
    INDEX idx_created_date (created_date),
    INDEX idx_deleted_at (deleted_at),
    
    FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE shipments (
    id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    tracking_number VARCHAR(100) NULL UNIQUE,
    carrier_name VARCHAR(100) NOT NULL,
    carrier_code VARCHAR(50) NOT NULL,
    shipping_address JSON NOT NULL,
    shipping_method VARCHAR(50) NOT NULL,
    status ENUM('pending', 'picked_up', 'in_transit', 'out_for_delivery', 'delivered', 'failed', 'returned') NOT NULL DEFAULT 'pending',
    estimated_delivery_date DATETIME NULL,
    actual_delivery_date DATETIME NULL,
    shipping_cost DECIMAL(15,2) NOT NULL,
    deleted_at DATETIME NULL,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    last_modified_by VARCHAR(36),
    
    INDEX idx_order_id (order_id),
    INDEX idx_carrier_code (carrier_code),
    INDEX idx_status (status),
    INDEX idx_estimated_delivery_date (estimated_delivery_date),
    INDEX idx_actual_delivery_date (actual_delivery_date),
    INDEX idx_deleted_at (deleted_at),
    
    CONSTRAINT chk_shipping_cost CHECK (shipping_cost >= 0),
    CONSTRAINT chk_delivery_dates CHECK (
        (actual_delivery_date IS NULL) OR
        (actual_delivery_date >= created_date)
    )
) ENGINE=InnoDB;

CREATE TABLE shipment_logs (
    id VARCHAR(36) PRIMARY KEY,
    shipment_id VARCHAR(36) NOT NULL,
    status ENUM('pending', 'picked_up', 'in_transit', 'out_for_delivery', 'delivered', 'failed', 'returned') NOT NULL,
    location VARCHAR(255) NULL,
    timestamp DATETIME NOT NULL,
    note TEXT NULL,
    deleted_at DATETIME NULL,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    last_modified_by VARCHAR(36),
    INDEX idx_shipment_id (shipment_id),
    INDEX idx_status (status),
    INDEX idx_timestamp (timestamp),
    INDEX idx_deleted_at (deleted_at),
    FOREIGN KEY (shipment_id) REFERENCES shipments(id) ON DELETE RESTRICT
) ENGINE=InnoDB;