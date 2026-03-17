package org.example.vti_ecommerce_product_service.controllers;

import java.time.LocalDateTime;

import org.example.vti_ecommerce_product_service.dtos.responses.BaseResponse;
import org.example.vti_ecommerce_product_service.dtos.responses.ProductResponse;
import org.example.vti_ecommerce_product_service.services.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/api/public/products")
@RequiredArgsConstructor
@Validated
public class ProductController {
   
    private final ProductService productService;

    // Lấy danh sách sản phẩm có phân trang
    @RequestMapping("/")
    ResponseEntity<BaseResponse<Page<ProductResponse>>> getAllProduct(
        @RequestParam(name = "categoryId") String categoryId,
        @RequestParam(name = "minPrice") Double minPrice,
        @RequestParam(name = "maxPrice") Double maxPrice,
        Pageable pageable
    ) {
        return ResponseEntity.ok(
            BaseResponse.<Page<ProductResponse>>builder()
                .success(true)
                .message("Get all products successfully")
                .data(productService.getAllProducts(categoryId, minPrice, maxPrice, pageable))
                .timeStamp(LocalDateTime.now())
                .fieldErros(null)
                .build()
        );
    }

    // Lấy chi tiết một sản phẩm kèm variant và image

    // Lấy cây danh mục

    // Lấy các biến thể của sản phẩm

}
