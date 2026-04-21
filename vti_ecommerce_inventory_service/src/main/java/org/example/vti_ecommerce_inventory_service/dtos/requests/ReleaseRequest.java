package org.example.vti_ecommerce_inventory_service.dtos.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReleaseRequest {

    @NotBlank(message = "orderId is required")
    private String orderId;

    @NotEmpty(message = "items must not be empty")
    @Valid
    private List<ReleaseItem> items;

    @Getter
    @Setter
    public static class ReleaseItem {

        @NotBlank(message = "variantId is required")
        private String variantId;

        @Positive(message = "quantity must be positive")
        private Integer quantity;
    }
}