package org.example.vti_ecommerce_product_service.mappers;

import java.util.List;

import org.example.vti_ecommerce_product_service.dtos.responses.ProductResponse;
import org.example.vti_ecommerce_product_service.entities.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductResponse toResponse(Product product);

    Product toEntity(ProductResponse productResponse);

    List<ProductResponse> toListResponse(List<Product> products);
}
