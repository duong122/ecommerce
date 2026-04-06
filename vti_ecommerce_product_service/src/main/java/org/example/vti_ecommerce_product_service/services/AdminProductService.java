package org.example.vti_ecommerce_product_service.services;

import org.example.vti_ecommerce_product_service.dtos.requests.CreateProductRequest;
import org.example.vti_ecommerce_product_service.dtos.responses.ProductDetailResponse;

public interface AdminProductService {

    ProductDetailResponse createProduct(CreateProductRequest request);
    
}
