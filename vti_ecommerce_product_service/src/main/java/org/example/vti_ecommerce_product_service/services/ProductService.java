package org.example.vti_ecommerce_product_service.services;

import java.util.List;

import org.example.vti_ecommerce_product_service.dtos.responses.ProductResponse;
import org.example.vti_ecommerce_product_service.entities.Product;
import org.example.vti_ecommerce_product_service.entities.ProductVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface ProductService {

    Page<ProductResponse> getAllProducts(String category_id, Double minPrice, Double maxPrice, Pageable pageable);

    Product getProductById(String id);

    List<ProductVariant> getProductVariants(String id);
}
