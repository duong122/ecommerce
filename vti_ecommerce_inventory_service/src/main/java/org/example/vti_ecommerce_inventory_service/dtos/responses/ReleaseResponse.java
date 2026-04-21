package org.example.vti_ecommerce_inventory_service.dtos.responses;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ReleaseResponse {

    private Boolean success;
    private String orderId;
    private List<ReleaseItemResult> items;

    @Getter
    @Builder
    public static class ReleaseItemResult {
        private String variantId;
        private Integer requestedQuantity;
        private Boolean success;
        private String failureReason;
    }
}