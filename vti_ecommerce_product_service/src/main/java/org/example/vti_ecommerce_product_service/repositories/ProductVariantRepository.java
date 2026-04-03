package org.example.vti_ecommerce_product_service.repositories;

import java.util.List;

import org.example.vti_ecommerce_product_service.entities.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, String> {
    
    @Query("""
            SELECT v FROM ProductVariant v
            WHERE v.productId IN :productIds
            AND v.deletedAt IS NULL
            AND v.isActive = true
            """)
    List<ProductVariant> findActiveVariantByProductId(@Param("productId") List<String> productIds);

    
}
