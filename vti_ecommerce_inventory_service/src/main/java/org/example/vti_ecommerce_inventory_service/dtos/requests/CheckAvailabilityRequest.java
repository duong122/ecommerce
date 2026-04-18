package org.example.vti_ecommerce_inventory_service.dtos.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CheckAvailabilityRequest {

    @NotEmpty(message = "Items list must not be empty")
    @Valid
    private List<AvailabilityItem> items;

    @Getter
    @Setter
    public static class AvailabilityItem {

        @NotNull(message = "Variant ID must not be null")
        private String variantId;

        @NotNull(message = "Quantity must not be null")
        @Positive(message = "Quantity must be greater than 0")
        private Integer quantity;
    }
}