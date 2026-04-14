package org.example.vti_ecommerce_product_service.events;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class CategoryCreatedEvent {
    private String categoryId;
    private String name;
    private String slug;
    private String parentId;
    private Boolean isActive;
    private Instant createdAt;
}