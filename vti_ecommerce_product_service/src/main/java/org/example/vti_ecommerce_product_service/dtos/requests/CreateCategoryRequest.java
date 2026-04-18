package org.example.vti_ecommerce_product_service.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCategoryRequest {

    @NotBlank(message = "Category name must not be blank")
    @Size(min = 2, max = 255, message = "Category name must be between 2 and 255 characters")
    private String name;

    private String parentId;

    private String description;

    private String imageUrl;

    private Integer sortOrder;

    private Boolean isActive;
}