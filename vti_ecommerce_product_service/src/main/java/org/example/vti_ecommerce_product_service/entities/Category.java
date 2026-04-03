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
@Table(name = "categories")
public class Category extends BaseEntity {
    
    @NotEmpty
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @NotEmpty
    @Column(name = "slug", nullable = false, unique = true)
    private String slug;

    private String description;
    
    @Column(name = "parent_id")
    private String parentId;

    @Column(name = "img_url")
    private String imgUrl;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
