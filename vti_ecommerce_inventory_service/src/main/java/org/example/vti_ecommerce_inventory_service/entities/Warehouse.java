package org.example.vti_ecommerce_inventory_service.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "warehouses")
public class Warehouse extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 500)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "warehouse_type", nullable = false)
    private WarehouseType warehouseType;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "manager_id", length = 36)
    private String managerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WarehouseStatus status;

    @Column(name = "contact_info", columnDefinition = "JSON")
    private String contactInfo;

    public enum WarehouseType {
        distribution_center, fulfillment_center, store
    }

    public enum WarehouseStatus {
        active, inactive, maintenance
    }
}