package org.example.vti_ecommerce_inventory_service.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "inventories")
public class Inventory extends BaseEntity {

    @Column(name = "variant_id", nullable = false, length = 36)
    private String variantId;

    @Column(name = "warehouse_id", nullable = false, length = 36)
    private String warehouseId; 

    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity;

    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;
}