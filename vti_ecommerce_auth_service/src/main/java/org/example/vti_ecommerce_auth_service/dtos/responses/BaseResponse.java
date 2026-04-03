package org.example.vti_ecommerce_auth_service.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class BaseResponse<T> {
    private boolean success;
    private T data;
    private String message;
    private Map<String, String> fieldErrors;
    private LocalDateTime timestamp;
}
