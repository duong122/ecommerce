package org.example.vti_ecommerce_product_service.mappers;

import org.example.vti_ecommerce_product_service.dtos.responses.CategoryResponse;
import org.example.vti_ecommerce_product_service.entities.Category;
import org.example.vti_ecommerce_product_service.events.CategoryCreatedEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryResponse toResponse(Category category);

    @Mapping(source = "id", target = "categoryId")
    @Mapping(source = "createdDate", target = "createdAt")
    CategoryCreatedEvent toCreatedEvent(Category category);
}