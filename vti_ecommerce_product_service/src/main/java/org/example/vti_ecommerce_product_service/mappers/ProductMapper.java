package org.example.vti_ecommerce_product_service.mappers;

import java.util.List;

import org.example.vti_ecommerce_product_service.dtos.responses.ProductSummaryResponse;
import org.example.vti_ecommerce_product_service.entities.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductSummaryResponse toResponse(Product product);

    Product toEntity(ProductSummaryResponse productResponse);

    List<ProductSummaryResponse> toListResponse(List<Product> products);
}
