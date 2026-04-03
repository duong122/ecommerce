package org.example.vti_ecommerce_product_service.exceptions;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.example.vti_ecommerce_product_service.dtos.responses.BaseResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<BaseResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(
            cv -> {
                String fields = cv.getPropertyPath().toString();
                errors.put(fields, cv.getMessage());
            }
        );

        return ResponseEntity.badRequest().body(
            BaseResponse.<Void>builder()
                .success(false)
                .data(null)
                .message("Validation input error")
                .fieldErros(errors)
                .timeStamp(LocalDateTime.now())
                .build()    
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BaseResponse<Void>> handleIlligalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(
            BaseResponse.<Void>builder()
                .success(false)
                .data(null)
                .message(ex.getMessage())
                .fieldErros(null)
                .timeStamp(LocalDateTime.now())
                .build()
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<BaseResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.badRequest().body(
            BaseResponse.<Void>builder()
                .success(false)
                .message(ex.getMessage())
                .data(null)
                .fieldErros(null)
                .timeStamp(LocalDateTime.now())
                .build()   
        );
    }
}
