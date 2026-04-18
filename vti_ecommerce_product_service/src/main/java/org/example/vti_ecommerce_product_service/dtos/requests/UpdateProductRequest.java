package org.example.vti_ecommerce_product_service.dtos.requests;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProductRequest {

    private String categoryId;

    @Size(min = 2, max = 255, message = "Product name must be between 2 and 255 characters")
    private String name;

    private String description;

    private Boolean isActive;
}