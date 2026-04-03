package org.example.vti_ecommerce_product_service.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductSummaryResponse {
    private String id;
    private String name;
    private String slug;
    private String categoryId;
    private String primaryImageUrl;
    private Double minPrice;
    private Double maxPrice;
    private Integer variantCount;
}
