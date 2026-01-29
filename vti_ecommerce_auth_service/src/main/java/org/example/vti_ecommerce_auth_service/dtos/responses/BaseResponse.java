package org.example.vti_ecommerce_auth_service.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class BaseResponse<T> {
    private T data;
    private String message;
    private Map<String, String> fieldErrors;
}
