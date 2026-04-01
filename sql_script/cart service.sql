CREATE DATABASE IF NOT EXISTS cart_service 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE cart_service;

CREATE TABLE carts (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NULL ,
    deleted_at DATETIME NULL,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    last_modified_by VARCHAR(36),
    
    INDEX idx_user_id (user_id),
    INDEX idx_last_modified (last_modified_date),
    INDEX idx_deleted_at (deleted_at)
    
) ENGINE=InnoDB;

-- Bảng cart_items: Chi tiết sản phẩm trong giỏ hàng
CREATE TABLE cart_items (
    id VARCHAR(36) PRIMARY KEY,
    cart_id VARCHAR(36) NOT NULL,
    variant_id VARCHAR(36) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    added_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    last_modified_by VARCHAR(36),
    
    INDEX idx_cart_id (cart_id),
    INDEX idx_variant_id (variant_id),
    INDEX idx_added_at (added_at),
    INDEX idx_deleted_at (deleted_at),
    
    UNIQUE KEY uk_cart_variant (cart_id, variant_id, deleted_at),
    
    FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    CONSTRAINT chk_quantity CHECK (quantity > 0)
) ENGINE=InnoDB;