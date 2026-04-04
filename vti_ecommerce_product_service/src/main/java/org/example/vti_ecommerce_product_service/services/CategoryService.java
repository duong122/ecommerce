package org.example.vti_ecommerce_product_service.services;

import java.util.List;

import org.example.vti_ecommerce_product_service.dtos.responses.CategoryTreeResponse;

public interface CategoryService {
    
    List<CategoryTreeResponse> getAllCategoy();
}
