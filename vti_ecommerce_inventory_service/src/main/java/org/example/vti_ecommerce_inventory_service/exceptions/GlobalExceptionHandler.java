package org.example.vti_ecommerce_inventory_service.exceptions;

import java.time.LocalDateTime;

import org.example.vti_ecommerce_inventory_service.dtos.responses.BaseResponse;
import org.example.vti_ecommerce_inventory_service.dtos.responses.ReleaseResponse;
import org.example.vti_ecommerce_inventory_service.dtos.responses.ReserveResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InventoryReservationException.class)
    public ResponseEntity<BaseResponse<ReserveResponse>> handleReservationException(
            InventoryReservationException ex) {

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                BaseResponse.<ReserveResponse>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .data(ReserveResponse.builder()
                                .success(false)
                                .items(ex.getItemResults())
                                .build())
                        .timeStamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(InventoryReleaseException.class)
    public ResponseEntity<BaseResponse<ReleaseResponse>> handleReleaseException(
            InventoryReleaseException ex) {

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                BaseResponse.<ReleaseResponse>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .data(ReleaseResponse.builder()
                                .success(false)
                                .items(ex.getItemResults())
                                .build())
                        .timeStamp(LocalDateTime.now())
                        .build());
    }
}
