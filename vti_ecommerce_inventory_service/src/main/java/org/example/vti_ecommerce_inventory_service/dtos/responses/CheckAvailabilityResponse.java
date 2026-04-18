package org.example.vti_ecommerce_inventory_service.dtos.responses;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class CheckAvailabilityResponse {

    private Boolean available;

    private List<AvailabilityItemResult> items;

    @Getter
    @Setter
    @Builder
    public static class AvailabilityItemResult {
        private String variantId;
        private Integer requestedQuantity;
        private Integer availableQuantity;
        private Boolean isAvailable;
    }
}