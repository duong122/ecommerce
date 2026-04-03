package org.example.vti_ecommerce_product_service.services;

import org.example.vti_ecommerce_product_service.dtos.requests.ProductFilterRequest;
import org.example.vti_ecommerce_product_service.dtos.responses.PagedResponse;
import org.example.vti_ecommerce_product_service.dtos.responses.ProductDetailResponse;
import org.example.vti_ecommerce_product_service.dtos.responses.ProductSummaryResponse;


public interface ProductService {

    PagedResponse<ProductSummaryResponse> getProducts(ProductFilterRequest productFilterRequest);

    ProductDetailResponse getProductById(String id);

}
