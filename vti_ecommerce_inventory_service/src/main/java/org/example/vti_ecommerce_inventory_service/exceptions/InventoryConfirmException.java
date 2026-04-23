package org.example.vti_ecommerce_inventory_service.exceptions;

import lombok.Getter;
import org.example.vti_ecommerce_inventory_service.dtos.responses.ConfirmResponse;

import java.util.List;

@Getter
public class InventoryConfirmException extends RuntimeException {

    private final List<ConfirmResponse.ConfirmItemResult> itemResults;

    public InventoryConfirmException(String message,
                                     List<ConfirmResponse.ConfirmItemResult> itemResults) {
        super(message);
        this.itemResults = itemResults;
    }
}