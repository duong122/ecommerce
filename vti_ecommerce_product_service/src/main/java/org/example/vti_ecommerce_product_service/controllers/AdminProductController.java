package org.example.vti_ecommerce_product_service.controllers;

import java.time.LocalDateTime;

import org.example.vti_ecommerce_product_service.dtos.requests.CreateProductRequest;
import org.example.vti_ecommerce_product_service.dtos.responses.BaseResponse;
import org.example.vti_ecommerce_product_service.dtos.responses.ProductDetailResponse;
import org.example.vti_ecommerce_product_service.services.AdminProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/admin/products/")
public class AdminProductController {

    private final AdminProductService adminProductService;

    @PostMapping("/")
    public ResponseEntity<BaseResponse<ProductDetailResponse>> createProduct(
            @Valid @RequestBody CreateProductRequest request) {

        ProductDetailResponse result = adminProductService.createProduct(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                BaseResponse.<ProductDetailResponse>builder()
                        .success(true)
                        .message("Product created successfully")
                        .data(result)
                        .fieldErros(null)
                        .timeStamp(LocalDateTime.now())
                        .build());
    }
}
