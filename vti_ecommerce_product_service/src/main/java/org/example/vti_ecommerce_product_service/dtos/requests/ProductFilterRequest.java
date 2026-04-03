package org.example.vti_ecommerce_product_service.dtos.requests;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductFilterRequest {
    
    @NotEmpty(message = "category id must not be empty")
    private String categoryId;

    @Min(value = 0, message = "min_price must be positive")
    private Double minPrice;

    @Min(value = 0, message = "max_price must be positive")
    private Double maxPrice;

    private String sortBy = "newest";

    @Min(value = 0)
    private Integer page = 0;

    @Min(value = 1)
    @Max(value = 100)
    private Integer size = 20;
}
