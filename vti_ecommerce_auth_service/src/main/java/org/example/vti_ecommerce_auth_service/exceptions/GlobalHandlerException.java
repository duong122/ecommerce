package org.example.vti_ecommerce_auth_service.exceptions;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.example.vti_ecommerce_auth_service.dtos.responses.BaseResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;

@RestControllerAdvice
public class GlobalHandlerException {
    
    @ExceptionHandler(AppException.class)
    public ResponseEntity<BaseResponse<Object>> handleAppException(AppException e, ServletWebRequest servletWebRequest) {
        ErrorCode errorCode = e.getErrorCode();
        
        return ResponseEntity
                    .status(errorCode.getHttpStatus())
                    .body(
                        BaseResponse.<Object>builder()
                            .success(false)
                            .message(e.getMessage())
                            .data(null)
                            .fieldErrors(e.getFieldErrors())
                            .timestamp(LocalDateTime.now())
                            .build()
                    );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Object>> handleArgumentNotValidException(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();
        String filedName = "";
        String fieldMessage = "";

        for (FieldError field : exception.getBindingResult().getFieldErrors()) {
            filedName = field.getField();
            fieldMessage = field.getDefaultMessage();
            errors.put(filedName, fieldMessage);
        }

        return ResponseEntity.badRequest().body(
            BaseResponse.<Object>builder()
                .success(false)
                .message(exception.getMessage())
                .data(null)
                .timestamp(LocalDateTime.now())
                .fieldErrors(errors)
                .build()
        );

    }
}
