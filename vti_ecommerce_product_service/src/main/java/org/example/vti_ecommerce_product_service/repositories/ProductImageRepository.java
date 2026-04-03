package org.example.vti_ecommerce_product_service.repositories;

import java.util.List;

import org.example.vti_ecommerce_product_service.entities.ProductImage;
import org.example.vti_ecommerce_product_service.projections.ProductImageProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, String> {

    @Query("""
            SELECT pi FROM ProductImage pi
            WHERE pi.productId IN :productIds
            AND pi.isPrimary = true
            AND pi.deletedAt IS NULL
            """)
    List<ProductImage> findByProductIdsAndIsPrimaryTrueAndDeletedAtIsNull(List<String> productIds);

    @Query("""
        SELECT pi FROM ProductImage pi  
        WHERE pi.productId IN :productIds
            AND pi.isPrimary = true
            AND pi.variantId IS NULL
            AND pi.deletedAt IS NULL
        """)
    List<ProductImage> findPrimaryImagesByProductIds(@Param("productIds") List<String> productIds); 

    @Query(value = """
        SELECT  pi.id         AS imageId,
                pi.variant_id AS variantId,
                pi.url        AS imageUrl,
                pi.alt_text   AS altText,
                pi.sort_order AS sortOrder,
                pi.is_primary AS isPrimary
        FROM product_images pi
        WHERE pi.product_id = :productId
            AND pi.deleted_at IS NULL
        ORDER BY pi.sort_order ASC
    """, nativeQuery = true)
    List<ProductImageProjection> findImagesByProductId(@Param("productId") String productId);
}
