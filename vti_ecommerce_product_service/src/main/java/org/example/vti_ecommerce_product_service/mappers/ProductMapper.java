package org.example.vti_ecommerce_product_service.mappers;

import java.util.List;

import org.example.vti_ecommerce_product_service.dtos.requests.UpdateProductRequest;
import org.example.vti_ecommerce_product_service.dtos.responses.ProductDetailResponse;
import org.example.vti_ecommerce_product_service.dtos.responses.ProductSummaryResponse;
import org.example.vti_ecommerce_product_service.entities.Product;
import org.example.vti_ecommerce_product_service.events.ProductCreatedEvent;
import org.example.vti_ecommerce_product_service.events.ProductUpdatedEvent;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductSummaryResponse toResponse(Product product);

    Product toEntity(ProductSummaryResponse productResponse);

    List<ProductSummaryResponse> toListResponse(List<Product> products);

    ProductDetailResponse toDetailResponse(Product product);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateProductFromRequest(UpdateProductRequest request, @MappingTarget Product product);

    @Mapping(source = "createdDate", target = "createdAt")
    ProductCreatedEvent toCreatedEvent(Product product);

    @Mapping(source = "lastModifiedDate", target = "updatedAt")
    ProductUpdatedEvent toUpdatedEvent(Product product);
}
