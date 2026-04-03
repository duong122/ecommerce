package org.example.vti_ecommerce_product_service.projections;

public interface ProductVariantProjection {
    String getVariantId();
    String getSku();
    Double getPrice();
    String getColor();
    String getSize();
    Double getWeight();
    Boolean getVariantIsActive();
}