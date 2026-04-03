package org.example.vti_ecommerce_product_service.entities;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product extends BaseEntity {
    
    @NotEmpty
    @Column(name = "category_id", nullable = false)
    private String categoryId;

    @NotEmpty(message = "Product's name must not be empty")
    private String name;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    private String slug;

    private String description;

}
