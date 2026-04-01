CREATE DATABASE IF NOT EXISTS promotion_service 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE promotion_service;

CREATE TABLE promotions (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    promotion_type ENUM('voucher', 'flash_sale', 'bundle', 'buy_x_get_y') NOT NULL,
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    is_flash_sale BOOLEAN NOT NULL DEFAULT FALSE,
    flash_sale_stock INT NULL,
    status ENUM('draft', 'active', 'inactive', 'expired') NOT NULL DEFAULT 'draft',
    deleted_at DATETIME NULL,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    last_modified_by VARCHAR(36),
    
    INDEX idx_promotion_type (promotion_type),
    INDEX idx_status (status),
    INDEX idx_is_flash_sale (is_flash_sale),
    INDEX idx_start_end_date (start_date, end_date),
    INDEX idx_deleted_at (deleted_at),
    
    CONSTRAINT chk_dates CHECK (end_date > start_date),
    CONSTRAINT chk_flash_sale_stock CHECK (
        (is_flash_sale = FALSE AND flash_sale_stock IS NULL) OR
        (is_flash_sale = TRUE AND flash_sale_stock > 0)
    )
) ENGINE=InnoDB;

CREATE TABLE vouchers (
    id VARCHAR(36) PRIMARY KEY,
    promotion_id VARCHAR(36) NOT NULL,
    voucher_code VARCHAR(50) NOT NULL UNIQUE,
    discount_type ENUM('percentage', 'fixed_amount') NOT NULL,
    discount_value DECIMAL(15,2) NOT NULL,
    max_discount_amount DECIMAL(15,2) NULL,
    min_order_value DECIMAL(15,2) NOT NULL DEFAULT 0,
    total_quantity INT NOT NULL,
    reserved_quantity INT NOT NULL DEFAULT 0,
    used_quantity INT NOT NULL DEFAULT 0,
    available_quantity INT NOT NULL,
    valid_from DATETIME NOT NULL,
    valid_to DATETIME NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at DATETIME NULL,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    last_modified_by VARCHAR(36),
    
    INDEX idx_promotion_id (promotion_id),
    INDEX idx_voucher_code (voucher_code),
    INDEX idx_is_active (is_active),
    INDEX idx_valid_dates (valid_from, valid_to),
    INDEX idx_available_quantity (available_quantity),
    INDEX idx_deleted_at (deleted_at),
    
    FOREIGN KEY (promotion_id) REFERENCES promotions(id) ON DELETE RESTRICT,
    
    CONSTRAINT chk_voucher_dates CHECK (valid_to > valid_from),
    CONSTRAINT chk_discount_value CHECK (discount_value > 0),
    CONSTRAINT chk_voucher_quantities CHECK (
        total_quantity >= 0 
        AND reserved_quantity >= 0 
        AND used_quantity >= 0
        AND available_quantity >= 0
        AND available_quantity = total_quantity - reserved_quantity - used_quantity
    )
) ENGINE=InnoDB;

CREATE TABLE promotion_items (
    id VARCHAR(36) PRIMARY KEY,
    promotion_id VARCHAR(36) NOT NULL,
    variant_id VARCHAR(36) NOT NULL,
    flash_sale_price DECIMAL(15,2) NULL,
    flash_sale_stock INT NULL,
    deleted_at DATETIME NULL,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    last_modified_by VARCHAR(36),
    
    INDEX idx_promotion_id (promotion_id),
    INDEX idx_variant_id (variant_id),
    INDEX idx_deleted_at (deleted_at),
    
	UNIQUE KEY uk_promotion_variant (promotion_id, variant_id),
    FOREIGN KEY (promotion_id) REFERENCES promotions(id) ON DELETE CASCADE,
    CONSTRAINT chk_flash_sale_fields CHECK (
        (flash_sale_price IS NULL AND flash_sale_stock IS NULL) OR
        (flash_sale_price > 0 AND flash_sale_stock > 0)
    )
) ENGINE=InnoDB;

CREATE TABLE voucher_usage_history (
    id VARCHAR(36) PRIMARY KEY,
    voucher_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    order_id VARCHAR(36) NOT NULL,
    discount_amount DECIMAL(15,2) NOT NULL,
    used_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    last_modified_by VARCHAR(36),
    
    INDEX idx_voucher_id (voucher_id),
    INDEX idx_user_id (user_id),
    INDEX idx_order_id (order_id),
    INDEX idx_used_at (used_at),
    INDEX idx_deleted_at (deleted_at),
    
    FOREIGN KEY (voucher_id) REFERENCES vouchers(id) ON DELETE RESTRICT,
    CONSTRAINT chk_discount_amount CHECK (discount_amount >= 0)
) ENGINE=InnoDB;