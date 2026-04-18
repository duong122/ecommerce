package org.example.vti_ecommerce_product_service.dtos.responses;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class CategoryResponse {
    private String id;
    private String name;
    private String slug;
    private String parentId;
    private String description;
    private String imageUrl;
    private Integer sortOrder;
    private Boolean isActive;
    private Instant createdDate;
    private Instant lastModifiedDate;
}