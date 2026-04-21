package org.example.vti_ecommerce_inventory_service.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.vti_ecommerce_inventory_service.dtos.requests.CheckAvailabilityRequest;
import org.example.vti_ecommerce_inventory_service.dtos.requests.ReleaseRequest;
import org.example.vti_ecommerce_inventory_service.dtos.requests.ReserveRequest;
import org.example.vti_ecommerce_inventory_service.dtos.responses.BaseResponse;
import org.example.vti_ecommerce_inventory_service.dtos.responses.CheckAvailabilityResponse;
import org.example.vti_ecommerce_inventory_service.dtos.responses.ReleaseResponse;
import org.example.vti_ecommerce_inventory_service.dtos.responses.ReserveResponse;
import org.example.vti_ecommerce_inventory_service.services.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/internal/inventory")
@RequiredArgsConstructor
public class InternalInventoryController {

        private final InventoryService inventoryService;
        private static final String INTERNAL_SERVICE_HEADER = "X-Internal-Service";

        @PostMapping("/check-availability")
        public ResponseEntity<BaseResponse<CheckAvailabilityResponse>> checkAvailability(
                        @RequestHeader(INTERNAL_SERVICE_HEADER) String internalHeader,
                        @Valid @RequestBody CheckAvailabilityRequest request) {

                if (!"true".equals(internalHeader)) {
                        return ResponseEntity.status(403).body(
                                        BaseResponse.<CheckAvailabilityResponse>builder()
                                                        .success(false)
                                                        .message("Access denied: internal service only")
                                                        .timeStamp(LocalDateTime.now())
                                                        .build());
                }

                CheckAvailabilityResponse data = inventoryService.checkAvailability(request);

                return ResponseEntity.ok(
                                BaseResponse.<CheckAvailabilityResponse>builder()
                                                .success(true)
                                                .message("Availability checked successfully")
                                                .data(data)
                                                .timeStamp(LocalDateTime.now())
                                                .build());
        }

        @PostMapping("/reserve")
        public ResponseEntity<BaseResponse<ReserveResponse>> reserve(
                        @RequestHeader(INTERNAL_SERVICE_HEADER) String internalHeader,
                        @Valid @RequestBody ReserveRequest request) {

                if (!"true".equals(internalHeader)) {
                        return ResponseEntity.status(403).body(
                                        BaseResponse.<ReserveResponse>builder()
                                                        .success(false)
                                                        .message("Access denied: internal service only")
                                                        .timeStamp(LocalDateTime.now())
                                                        .build());
                }

                ReserveResponse data = inventoryService.reserve(request);

                return ResponseEntity.ok(
                                BaseResponse.<ReserveResponse>builder()
                                                .success(true)
                                                .message("Stock reserved successfully")
                                                .data(data)
                                                .timeStamp(LocalDateTime.now())
                                                .build());
        }

        @PostMapping("/release")
        public ResponseEntity<BaseResponse<ReleaseResponse>> release(
                        @RequestHeader(INTERNAL_SERVICE_HEADER) String internalHeader,
                        @Valid @RequestBody ReleaseRequest request) {

                if (!"true".equals(internalHeader)) {
                        return ResponseEntity.status(403).body(
                                        BaseResponse.<ReleaseResponse>builder()
                                                        .success(false)
                                                        .message("Access denied: internal service only")
                                                        .timeStamp(LocalDateTime.now())
                                                        .build());
                }

                ReleaseResponse data = inventoryService.release(request);

                return ResponseEntity.ok(
                                BaseResponse.<ReleaseResponse>builder()
                                                .success(true)
                                                .message("Stock released successfully")
                                                .data(data)
                                                .timeStamp(LocalDateTime.now())
                                                .build());
        }
}