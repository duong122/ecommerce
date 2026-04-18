package org.example.vti_ecommerce_product_service.projections;

public interface CategoryProjection {
    String getId();
    String getName();
    String getSlug();
    String getParentId();
    Integer getSortOrder();
    String getImgUrl();
    String isActive();
}
