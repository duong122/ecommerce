package org.example.vti_ecommerce_product_service.dtos.responses;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CategoryTreeResponse {
    private String id;
    private String name;
    private String slug;
    private String thumbnailUrl;
    private Integer sortOrder;
    private List<CategoryTreeResponse> children;
}
