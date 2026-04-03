package org.example.vti_ecommerce_product_service.repositories;


import org.example.vti_ecommerce_product_service.entities.Product;
import org.example.vti_ecommerce_product_service.projections.ProductSummaryProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface ProductRepository extends JpaRepository<Product, String>, JpaSpecificationExecutor<Product> {

    // Query chính lấy danh sách product với aggregate từ variants
    @Query(value = """
        SELECT 
            p.id AS id,
            p.name AS name,
            p.slug AS slug,
            p.category_id AS categoryId,
            MIN(pv.price) AS minPrice,
            MAX(pv.price) AS maxPrice,
            COUNT(pv.id) AS variantCount,
            p.created_date AS createdDate
        FROM products p
        JOIN product_variants pv ON pv.product_id = p.id
            AND pv.deleted_at IS NULL
            AND pv.is_active = true
        WHERE p.deleted_at IS NULL
            AND (:categoryId IS NULL OR p.category_id = :categoryId)
        GROUP BY p.id, p.name, p.slug, p.category_id, p.created_date
        HAVING (:minPrice IS NULL OR MIN(pv.price) >= :minPrice)
            AND (:maxPrice IS NULL OR MIN(pv.price) <= :maxPrice)
        """,
        countQuery = """
        SELECT COUNT(DISTINCT p.id)
        FROM products p
        JOIN product_variants pv ON pv.product_id = p.id
            AND pv.deleted_at IS NULL
            AND pv.is_active = true
        WHERE p.deleted_at IS NULL
            AND (:categoryId IS NULL OR p.category_id = :categoryId)
        HAVING (:minPrice IS NULL OR MIN(pv.price) >= :minPrice)
            AND (:maxPrice IS NULL OR MIN(pv.price) <= :maxPrice)
        """,
        nativeQuery = true)
    Page<ProductSummaryProjection> findProductsWithFilter(
        @Param("categoryId") String categoryId,
        @Param("minPrice") Double minPrice,
        @Param("maxPrice") Double maxPrice,
        Pageable pageable
    );
}
