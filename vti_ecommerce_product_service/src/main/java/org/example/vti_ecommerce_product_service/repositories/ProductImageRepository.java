package org.example.vti_ecommerce_product_service.repositories;

import java.util.List;

import org.example.vti_ecommerce_product_service.entities.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.lettuce.core.dynamic.annotation.Param;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, String> {

    @Query("""
            SELECT pi FROM ProductImage pi
            WHERE pi.productId IN :productIds
            AND pi.isPrimary = true
            AND pi.deletedAt IS NULL
            """)
    List<ProductImage> findByProductIdsAndIsPrimaryTrueAndDeletedAtIsNull(List<String> productIds);

    // Query lấy primary image theo batch — tránh N+1
    @Query("""
        SELECT pi FROM ProductImage pi  
        WHERE pi.productId IN :productIds
            AND pi.isPrimary = true
            AND pi.variantId IS NULL
            AND pi.deletedAt IS NULL
        """)
    List<ProductImage> findPrimaryImagesByProductIds(@Param("productIds") List<String> productIds); 
}
