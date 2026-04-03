package org.example.vti_ecommerce_product_service.projections;

public interface ProductBaseProjection {
    String getId();
    String getCategoryId();
    String getName();
    String getSlug();
    String getDescription();
    Boolean getIsActive();
}