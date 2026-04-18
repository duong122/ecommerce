package org.example.vti_ecommerce_inventory_service.services;

import org.example.vti_ecommerce_inventory_service.dtos.requests.CheckAvailabilityRequest;
import org.example.vti_ecommerce_inventory_service.dtos.responses.CheckAvailabilityResponse;

public interface InventoryService {

    CheckAvailabilityResponse checkAvailability(CheckAvailabilityRequest request);
    
}