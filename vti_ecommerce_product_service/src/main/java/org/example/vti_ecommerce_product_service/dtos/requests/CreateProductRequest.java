package org.example.vti_ecommerce_product_service.dtos.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class CreateProductRequest {

    @NotBlank(message = "categoryId must not be blank")
    private String categoryId;

    @NotBlank(message = "name must not be blank")
    private String name;

    private String description;

    @NotEmpty(message = "Product must have at least one variant")
    @Valid
    private List<CreateVariantRequest> variants;
}