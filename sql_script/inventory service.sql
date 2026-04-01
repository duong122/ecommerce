CREATE DATABASE IF NOT EXISTS inventory_service 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE inventory_service;

CREATE TABLE warehouses (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE COMMENT 'Mã kho để integration với WMS',
    location VARCHAR(500) NOT NULL COMMENT 'Địa chỉ chi tiết của kho',
    warehouse_type ENUM('distribution_center', 'fulfillment_center', 'store') NOT NULL,
    capacity INT NOT NULL COMMENT 'Sức chứa tối đa (số lượng SKU hoặc m3)',
    manager_id VARCHAR(36) COMMENT 'ID của warehouse manager từ User Service',
    status ENUM('active', 'inactive', 'maintenance') NOT NULL DEFAULT 'active',
    contact_info JSON COMMENT '{"phone": "0123456789", "email": "warehouse@example.com"}',
    deleted_at DATETIME NULL,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    last_modified_by VARCHAR(36),
    
    INDEX idx_code (code),
    INDEX idx_status (status),
    INDEX idx_warehouse_type (warehouse_type),
    INDEX idx_deleted_at (deleted_at)
) ENGINE=InnoDB COMMENT='Quản lý danh sách kho hàng';

-- Bảng inventories: Tồn kho thực tế tại từng warehouse
CREATE TABLE inventories (
    id VARCHAR(36) PRIMARY KEY,
    variant_id VARCHAR(36) NOT NULL,
    warehouse_id VARCHAR(36) NOT NULL,
    total_quantity INT NOT NULL DEFAULT 0,
    reserved_quantity INT NOT NULL DEFAULT 0,
    available_quantity INT NOT NULL DEFAULT 0,
    deleted_at DATETIME NULL,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    last_modified_by VARCHAR(36),
    
    UNIQUE KEY uk_variant_warehouse (variant_id, warehouse_id),
    
    INDEX idx_variant_id (variant_id),
    INDEX idx_warehouse_id (warehouse_id),
    INDEX idx_available_quantity (available_quantity),
    INDEX idx_deleted_at (deleted_at),
    
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id) ON DELETE RESTRICT,
    
    CONSTRAINT chk_quantities CHECK (
        total_quantity >= 0 
        AND reserved_quantity >= 0 
        AND available_quantity >= 0
        AND available_quantity = total_quantity - reserved_quantity
    )
) ENGINE=InnoDB;


CREATE TABLE inventory_transactions (
    id VARCHAR(36) PRIMARY KEY,
    inventory_id VARCHAR(36) NOT NULL,
    variant_id VARCHAR(36) NOT NULL,
    warehouse_id VARCHAR(36) NOT NULL,
    transaction_type ENUM('import', 'export', 'transfer', 'adjustment', 'reserve', 'release') NOT NULL,
    quantity INT NOT NULL,
    reference_type ENUM('order', 'purchase_order', 'transfer', 'manual', 'return') NOT NULL,
    reference_id VARCHAR(36) NOT NULL COMMENT 'ID của order, purchase order, transfer order, etc.',
    note TEXT COMMENT 'Ghi chú lý do thay đổi',
    deleted_at DATETIME NULL,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    last_modified_by VARCHAR(36),
    
    INDEX idx_inventory_id (inventory_id),
    INDEX idx_variant_warehouse (variant_id, warehouse_id),
    INDEX idx_transaction_type (transaction_type),
    INDEX idx_reference (reference_type, reference_id),
    INDEX idx_created_date (created_date),
    
    FOREIGN KEY (inventory_id) REFERENCES inventories(id) ON DELETE RESTRICT,
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id) ON DELETE RESTRICT
) ENGINE=InnoDB COMMENT='Audit trail mọi thay đổi inventory';