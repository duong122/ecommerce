CREATE DATABASE IF NOT EXISTS order_service 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE order_service;


CREATE TABLE orders (
    id VARCHAR(36) PRIMARY KEY,
    order_code VARCHAR(50) NOT NULL UNIQUE,
    user_id VARCHAR(36) NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL,
    discount_amount DECIMAL(15,2) NOT NULL DEFAULT 0,
    tax_amount DECIMAL(15,2) NOT NULL DEFAULT 0,
    shipping_fee DECIMAL(15,2) NOT NULL DEFAULT 0,
    final_amount DECIMAL(15,2) NOT NULL,
    status ENUM('pending', 'confirmed', 'processing', 'shipped', 'delivered', 'cancelled', 'refunded') NOT NULL DEFAULT 'pending',
    shipping_address JSON NOT NULL,
    payment_method ENUM('cod', 'bank_transfer', 'vnpay', 'momo', 'zalopay') NOT NULL,
    order_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    customer_note TEXT,
    internal_note TEXT,
    deleted_at DATETIME NULL,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    last_modified_by VARCHAR(36),
    
    INDEX idx_order_code (order_code),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_order_date (order_date),
    INDEX idx_user_status (user_id, status),
    INDEX idx_deleted_at (deleted_at),
    
    CONSTRAINT chk_amounts CHECK (
        total_amount >= 0 
        AND discount_amount >= 0 
        AND tax_amount >= 0 
        AND shipping_fee >= 0
        AND final_amount >= 0
        AND final_amount = total_amount - discount_amount + tax_amount + shipping_fee
    )
) ENGINE=InnoDB;


CREATE TABLE order_items (
    id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    variant_id VARCHAR(36) NOT NULL,
    product_name VARCHAR(500) NOT NULL,
    variant_info JSON NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(15,2) NOT NULL,
    discount_amount DECIMAL(15,2) NOT NULL DEFAULT 0,
    final_price DECIMAL(15,2) NOT NULL,
    deleted_at DATETIME NULL,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    last_modified_by VARCHAR(36),
    
    INDEX idx_order_id (order_id),
    INDEX idx_variant_id (variant_id),
    INDEX idx_deleted_at (deleted_at),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE RESTRICT,
    
    CONSTRAINT chk_item_amounts CHECK (
        quantity > 0 
        AND unit_price >= 0 
        AND discount_amount >= 0
        AND final_price >= 0
        AND final_price = (unit_price * quantity) - discount_amount
    )
) ENGINE=InnoDB;

CREATE TABLE order_status_history (
    id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    from_status ENUM('pending', 'confirmed', 'processing', 'shipped', 'delivered', 'cancelled', 'refunded') NULL,
    to_status ENUM('pending', 'confirmed', 'processing', 'shipped', 'delivered', 'cancelled', 'refunded') NOT NULL,
    note TEXT,
    changed_by VARCHAR(36) NOT NULL,
    deleted_at DATETIME NULL,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    last_modified_by VARCHAR(36),
    
    INDEX idx_order_id (order_id),
    INDEX idx_to_status (to_status),
    INDEX idx_created_date (created_date),
    INDEX idx_changed_by (changed_by),
    
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE RESTRICT
) ENGINE=InnoDB;