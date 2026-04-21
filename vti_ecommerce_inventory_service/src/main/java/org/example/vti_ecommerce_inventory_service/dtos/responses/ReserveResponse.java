package org.example.vti_ecommerce_inventory_service.dtos.responses;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ReserveResponse {

    private Boolean success;
    private String orderId;
    private List<ReserveItemResult> items;

    @Getter
    @Builder
    public static class ReserveItemResult {
        private String variantId;
        private Integer requestedQuantity;
        private Integer reservedQuantity;   
        private Boolean success;
        private String failureReason;    
    }
}