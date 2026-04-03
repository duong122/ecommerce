package org.example.vti_ecommerce_product_service.dtos.responses;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@Builder
public class ProductDetailResponse {

    private String id;
    private String categoryId;
    private String name;
    private String slug;
    private String description;
    private Boolean isActive;
    private List<VariantResponse> variants;
    private List<ImageResponse> images;

    @Getter
    @Setter
    @Builder
    public static class VariantResponse {
        private String variantId;
        private String sku;
        private Double price;
        private String color;
        private String size;
        private Double weight;
        private Boolean isActive;
    }

    @Getter
    @Setter
    @Builder
    public static class ImageResponse {
        private String imageId;
        private String variantId;
        private String url;
        private String altText;
        private Integer sortOrder;
        private Boolean isPrimary;
    }
}