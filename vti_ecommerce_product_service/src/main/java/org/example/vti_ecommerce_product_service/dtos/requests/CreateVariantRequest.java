package org.example.vti_ecommerce_product_service.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class CreateVariantRequest {

    @NotBlank(message = "sku must not be blank")
    private String sku;

    @NotNull(message = "price must not be null")
    @Positive(message = "price must be greater than 0")
    private Double price;

    private String color;
    private String size;

    @NotNull(message = "weight must not be null")
    @Positive(message = "weight must be greater than 0")
    private Double weight;

    @NotEmpty
    private List<String> imageUrls;
}