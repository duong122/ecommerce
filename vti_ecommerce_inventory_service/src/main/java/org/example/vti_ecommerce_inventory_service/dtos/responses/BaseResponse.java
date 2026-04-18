package org.example.vti_ecommerce_inventory_service.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class BaseResponse<T> {
    private Boolean success;
    private T data;
    private String message;
    private Map<String, String> fieldErrors;
    private LocalDateTime timeStamp;
}