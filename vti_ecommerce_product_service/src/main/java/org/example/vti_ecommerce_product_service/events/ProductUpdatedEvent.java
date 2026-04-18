package org.example.vti_ecommerce_product_service.events;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class ProductUpdatedEvent {
    private String productId;
    private String name;
    private String slug;
    private String categoryId;
    private Boolean isActive;
    private Instant updatedAt;
}