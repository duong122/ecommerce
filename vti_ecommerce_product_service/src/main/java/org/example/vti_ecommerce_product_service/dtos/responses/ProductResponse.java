package org.example.vti_ecommerce_product_service.dtos.responses;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private String id;

    private String categoryId;

    private String name;

    private String description;

    private Instant createdDate;

    private Double price;
}
