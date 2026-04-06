package org.example.vti_ecommerce_product_service.repositories;

import java.util.List;

import org.example.vti_ecommerce_product_service.entities.ProductVariant;
import org.example.vti_ecommerce_product_service.projections.ProductVariantProjection;
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

    @Query(value = """
    SELECT
        pv.id        AS variantId,
        pv.sku       AS sku,
        pv.price     AS price,
        pv.color     AS color,
        pv.size      AS size,
        pv.weight    AS weight,
        pv.is_active AS variantIsActive
    FROM product_variants pv
    WHERE pv.product_id = :productId
        AND pv.deleted_at IS NULL
        AND pv.is_active = true
    """, nativeQuery = true)
    List<ProductVariantProjection> findVariantsByProductId(@Param("productId") String productId);

    Boolean existsBySkuAndDeletedAtIsNull(String sku);
}
