package org.example.vti_ecommerce_product_service.projections;

import java.time.LocalDateTime;

public interface ProductSummaryProjection {
    String getId();
    String getName();
    String getSlug();
    String getCategoryId();
    Double getMinPrice();
    Double getMaxPrice();
    Integer getVariantCount();
    LocalDateTime getCreatedDate();
}
