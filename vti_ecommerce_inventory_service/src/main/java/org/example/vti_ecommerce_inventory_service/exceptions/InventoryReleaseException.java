package org.example.vti_ecommerce_inventory_service.exceptions;

import lombok.Getter;
import org.example.vti_ecommerce_inventory_service.dtos.responses.ReleaseResponse;

import java.util.List;

@Getter
public class InventoryReleaseException extends RuntimeException {

    private final List<ReleaseResponse.ReleaseItemResult> itemResults;

    public InventoryReleaseException(String message,
                                     List<ReleaseResponse.ReleaseItemResult> itemResults) {
        super(message);
        this.itemResults = itemResults;
    }
}