package org.example.vti_ecommerce_product_service.repositories;

import java.util.List;
import java.util.Optional;

import org.example.vti_ecommerce_product_service.entities.Category;
import org.example.vti_ecommerce_product_service.projections.CategoryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String>  {

    @Query(value = """
    SELECT
        c.id         AS id,
        c.name       AS name,
        c.slug       AS slug,
        c.parent_id  AS parentId,
        c.sort_order AS sortOrder,
        c.is_active  AS isActive,
        c.image_url  AS imageUrl
    FROM categories c
    WHERE c.deleted_at IS NULL
        AND c.is_active = true
    ORDER BY c.sort_order ASC
    """, nativeQuery = true)
    List<CategoryProjection> findAllActiveCategories();

    Boolean existsActiveById(@Param("id") String id);

    Optional<Category> findByIdAndDeletedAtIsNull(String id);

    Boolean existsByNameAndDeletedAtIsNull(String name);

    Boolean existsBySlugAndDeletedAtIsNull(String slug);

    // Recursive CTE tính depth của category cha — tận dụng index idx_categories_parent
    // Đi từ parentId lên root, đếm số bước
    // Trả về depth của parentId, nếu >= 3 thì reject vì con sẽ là cấp 4
    @Query(value = """
        WITH RECURSIVE category_depth AS (
            SELECT id, parent_id, 1 AS depth
            FROM categories
            WHERE id = :parentId
                AND deleted_at IS NULL
            
            UNION ALL
            
            SELECT c.id, c.parent_id, cd.depth + 1
            FROM categories c
            INNER JOIN category_depth cd ON c.id = cd.parent_id
            WHERE c.deleted_at IS NULL
        )
        SELECT MAX(depth) FROM category_depth
        """, nativeQuery = true)
    Integer calculateDepthByParentId(@Param("parentId") String parentId);
}
