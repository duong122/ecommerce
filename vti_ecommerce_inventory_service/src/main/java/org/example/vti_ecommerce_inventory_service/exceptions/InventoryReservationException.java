package org.example.vti_ecommerce_inventory_service.exceptions;

import lombok.Getter;
import org.example.vti_ecommerce_inventory_service.dtos.responses.ReserveResponse;

import java.util.List;

@Getter
public class InventoryReservationException extends RuntimeException {

    private final List<ReserveResponse.ReserveItemResult> itemResults;

    public InventoryReservationException(String message,
                                         List<ReserveResponse.ReserveItemResult> itemResults) {
        super(message);
        this.itemResults = itemResults;
    }
}