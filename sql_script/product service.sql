CREATE DATABASE IF NOT EXISTS product_service 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE product_service;

-- Bảng này có thể xóa decription field
CREATE TABLE categories (
    id VARCHAR(36) PRIMARY KEY ,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    parent_id VARCHAR(36) NULL,
    description TEXT,
    image_url VARCHAR(500),
    sort_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    deleted_at DATETIME NULL,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    last_modified_by VARCHAR(36),
    
    INDEX idx_parent_id (parent_id),
    INDEX idx_slug (slug),
    INDEX idx_is_active (is_active),
    INDEX idx_deleted_at (deleted_at),
    FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE products (
    id VARCHAR(36) PRIMARY KEY,
    category_id VARCHAR(36) NOT NULL,
    name VARCHAR(500) NOT NULL,
    slug VARCHAR(500) NOT NULL UNIQUE,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    deleted_at DATETIME NULL,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    last_modified_by VARCHAR(36),
    
    INDEX idx_category_id (category_id),
    INDEX idx_slug (slug),
    INDEX idx_is_active (is_active),
    INDEX idx_deleted_at (deleted_at),
    INDEX idx_created_date (created_date),
    FULLTEXT INDEX ft_name_description (name, description),
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE product_variants (
    id VARCHAR(36) PRIMARY KEY ,
    product_id VARCHAR(36) NOT NULL,
    sku VARCHAR(100) NOT NULL UNIQUE,
    price DECIMAL(15,2) NOT NULL,
    color VARCHAR(50),
    size VARCHAR(50),
    weight DECIMAL(10,2) COMMENT 'Trọng lượng tính bằng gram',
    is_active BOOLEAN DEFAULT TRUE,
    deleted_at DATETIME NULL,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    last_modified_by VARCHAR(36),
    
    INDEX idx_product_id (product_id),
    INDEX idx_sku (sku),
    INDEX idx_is_active (is_active),
    INDEX idx_deleted_at (deleted_at),
    INDEX idx_price (price),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE product_images (
    id VARCHAR(36) PRIMARY KEY ,
    product_id VARCHAR(36) NOT NULL,
    variant_id VARCHAR(36) NULL COMMENT 'NULL nếu là ảnh chung của product',
    url VARCHAR(500) NOT NULL,	
    alt_text VARCHAR(255),
    sort_order INT DEFAULT 0,
    is_primary BOOLEAN DEFAULT FALSE COMMENT 'Ảnh đại diện chính',
    deleted_at DATETIME NULL,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    last_modified_by VARCHAR(36),
    
    INDEX idx_product_id (product_id),
    INDEX idx_variant_id (variant_id),
    INDEX idx_is_primary (is_primary),
    INDEX idx_deleted_at (deleted_at),
    
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE product_audit_logs (
    id VARCHAR(36) PRIMARY KEY ,
    product_id VARCHAR(36) NULL,
    variant_id VARCHAR(36) NULL,
    field_name VARCHAR(100) NOT NULL COMMENT 'Tên field bị thay đổi',
    old_value TEXT COMMENT 'Giá trị cũ dạng JSON hoặc text',
    new_value TEXT COMMENT 'Giá trị mới dạng JSON hoặc text',
    action_type ENUM('CREATE', 'UPDATE', 'DELETE', 'RESTORE') NOT NULL,
    deleted_at DATETIME NULL,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    last_modified_by VARCHAR(36),
    
    INDEX idx_product_id (product_id),
    INDEX idx_variant_id (variant_id),
    INDEX idx_action_type (action_type),
    INDEX idx_created_date (created_date)
) ENGINE=InnoDB;

-- DROP DATABASE product_service;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE product_images;
TRUNCATE TABLE product_variants;
TRUNCATE TABLE products;
TRUNCATE TABLE categories;
SET FOREIGN_KEY_CHECKS = 1;

select * from categories;
select count(*) from products  group by products.id;

