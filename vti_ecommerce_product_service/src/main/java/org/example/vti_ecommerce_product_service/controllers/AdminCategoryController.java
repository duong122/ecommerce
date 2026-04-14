package org.example.vti_ecommerce_product_service.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.vti_ecommerce_product_service.dtos.requests.CreateCategoryRequest;
import org.example.vti_ecommerce_product_service.dtos.responses.BaseResponse;
import org.example.vti_ecommerce_product_service.dtos.responses.CategoryResponse;
import org.example.vti_ecommerce_product_service.services.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/v1/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {

        CategoryResponse data = categoryService.createCategory(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                BaseResponse.<CategoryResponse>builder()
                        .success(true)
                        .message("Category created successfully")
                        .data(data)
                        .timeStamp(LocalDateTime.now())
                        .build());
    }
}