package org.example.vti_ecommerce_inventory_service.services;

import org.example.vti_ecommerce_inventory_service.dtos.requests.CheckAvailabilityRequest;
import org.example.vti_ecommerce_inventory_service.dtos.requests.ConfirmRequest;
import org.example.vti_ecommerce_inventory_service.dtos.requests.ReleaseRequest;
import org.example.vti_ecommerce_inventory_service.dtos.requests.ReserveRequest;
import org.example.vti_ecommerce_inventory_service.dtos.responses.CheckAvailabilityResponse;
import org.example.vti_ecommerce_inventory_service.dtos.responses.ConfirmResponse;
import org.example.vti_ecommerce_inventory_service.dtos.responses.ReleaseResponse;
import org.example.vti_ecommerce_inventory_service.dtos.responses.ReserveResponse;

public interface InventoryService {

    CheckAvailabilityResponse checkAvailability(CheckAvailabilityRequest request);
    
    ReserveResponse reserve(ReserveRequest request);

    ReleaseResponse release(ReleaseRequest request);

    ConfirmResponse confirm(ConfirmRequest request);
}