package org.example.vti_ecommerce_product_service.services.impl;

import java.util.List;

import org.example.vti_ecommerce_product_service.dtos.responses.ProductResponse;
import org.example.vti_ecommerce_product_service.entities.Product;
import org.example.vti_ecommerce_product_service.entities.ProductVariant;
import org.example.vti_ecommerce_product_service.mappers.ProductMapper;
import org.example.vti_ecommerce_product_service.repositories.ProductRepository;
import org.example.vti_ecommerce_product_service.services.ProductService;
import jakarta.persistence.criteria.Join;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    
    private final ProductMapper productMapper;

    @Override
    public Page<ProductResponse> getAllProducts(String categoryId, Double minPrice, Double maxPrice,Pageable  pageable) {
       
        Specification<Product> spec = Specification.where(
            (root, query, cb) -> {
                return cb.isNotNull(root.get("deletedAt"));
            }
        );

        if (categoryId != null && !categoryId.isEmpty()) {
            spec.and((root, query, cb) -> cb.equal(root, categoryId));
        } 

        if (!minPrice.isNaN() || !maxPrice.isNaN()) {
            spec = spec.and((root, query, cb) -> {
                Join<Object, Object> variants = root.join("variants");

                if (!minPrice.isNaN() && !maxPrice.isNaN()) {
                    return cb.between(variants.get("price"), minPrice, maxPrice);
                } else if (!minPrice.isNaN()) {
                    return cb.greaterThan(variants.get("price"), minPrice);
                } else {
                    return cb.lessThanOrEqualTo(variants.get("price"), maxPrice);
                }
            });
        }

        Specification<Product> distincProduct = (root, query, cb) -> {
            query.distinct(true);
            return null;
        };

        spec = spec.and(distincProduct);

        Page<Product> productPage = productRepository.findAll(spec, pageable);

        return productPage.map(productMapper::toResponse);
    }

    @Override
    public Product getProductById(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getProductById'");
    }

    @Override
    public List<ProductVariant> getProductVariants(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getProductVariants'");
    }


    
}
