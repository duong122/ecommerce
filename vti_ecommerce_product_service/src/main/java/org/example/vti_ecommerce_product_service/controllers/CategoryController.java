package org.example.vti_ecommerce_product_service.controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.example.vti_ecommerce_product_service.dtos.responses.BaseResponse;
import org.example.vti_ecommerce_product_service.dtos.responses.CategoryTreeResponse;
import org.example.vti_ecommerce_product_service.services.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/categories/")
public class CategoryController {
    
    private final CategoryService categoryService;

    @GetMapping("/") 
    public ResponseEntity<BaseResponse<List<CategoryTreeResponse>>> getAllCategories() {

        return ResponseEntity.ok(
            BaseResponse.<List<CategoryTreeResponse>>builder()
                    .success(true)
                    .message("Get all categories successfully")
                    .data(categoryService.getAllCategoy())
                    .fieldErros(null)
                    .timeStamp(LocalDateTime.now())
                    .build()
        );
    }
}
