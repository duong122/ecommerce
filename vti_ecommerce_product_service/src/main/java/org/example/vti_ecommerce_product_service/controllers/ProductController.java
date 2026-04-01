package org.example.vti_ecommerce_product_service.controllers;

import java.time.LocalDateTime;

import org.example.vti_ecommerce_product_service.dtos.requests.ProductFilterRequest;
import org.example.vti_ecommerce_product_service.dtos.responses.BaseResponse;
import org.example.vti_ecommerce_product_service.dtos.responses.PagedResponse;
import org.example.vti_ecommerce_product_service.dtos.responses.ProductSummaryResponse;
import org.example.vti_ecommerce_product_service.services.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/api/public/products")
@RequiredArgsConstructor
@Validated
public class ProductController {
   
    private final ProductService productService;

    // Lấy danh sách sản phẩm có phân trang
    @GetMapping("/")
    ResponseEntity<BaseResponse<PagedResponse<ProductSummaryResponse>>> getProducts(
        @Valid @RequestBody ProductFilterRequest productFilterRequest
    ) {
        PagedResponse<ProductSummaryResponse> result = productService.getProducts(productFilterRequest);

        return ResponseEntity.ok(
            BaseResponse.<PagedResponse<ProductSummaryResponse>>builder()
                .success(true)
                .message("Get all products successfully")
                .data(result)
                .timeStamp(LocalDateTime.now())
                .fieldErros(null)
                .build()
        );
    }



}
