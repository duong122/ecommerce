package org.example.vti_ecommerce_product_service.repositories;

import java.util.List;

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
}
