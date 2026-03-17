package org.example.vti_ecommerce_product_service.dtos.responses;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class BaseResponse<T> {
    private Boolean success;
    private T data;
    private String message;
    private Map<String, String> fieldErros;
    private LocalDateTime timeStamp;
}
