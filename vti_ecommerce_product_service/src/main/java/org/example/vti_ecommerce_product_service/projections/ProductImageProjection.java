package org.example.vti_ecommerce_product_service.projections;

public interface ProductImageProjection {
    String getImageId();
    String getVariantId();
    String getImageUrl();
    String getAltText();
    Integer getSortOrder();
    Boolean getIsPrimary();
}
