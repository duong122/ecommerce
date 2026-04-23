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
public class ConfirmRequest {

    @NotBlank(message = "orderId is required")
    private String orderId;

    @NotEmpty(message = "items must not be empty")
    @Valid
    private List<ConfirmItem> items;

    @Getter
    @Setter
    public static class ConfirmItem {

        @NotBlank(message = "variantId is required")
        private String variantId;

        @Positive(message = "quantity must be positive")
        private Integer quantity;
    }
}