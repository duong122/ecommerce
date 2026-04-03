package org.example.vti_ecommerce_product_service.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotEmpty;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product_variants")
public class ProductVariant extends BaseEntity {

    @NotEmpty
    @Column(name = "product_id", nullable = false)
    private String productId;

    @NotEmpty
    @Column(unique = true, nullable = false)
    private String sku;

    @Column(nullable = false)
    private Double price;

    private String color;

    private String size;

    private Double weight;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}