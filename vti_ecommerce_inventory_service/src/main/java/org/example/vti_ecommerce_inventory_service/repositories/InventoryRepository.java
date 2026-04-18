package org.example.vti_ecommerce_inventory_service.repositories;

import org.example.vti_ecommerce_inventory_service.entities.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, String> {

    @Query(value = """
        SELECT i.variant_id, SUM(i.available_quantity) AS available_quantity
        FROM inventories i
        WHERE i.variant_id IN (:variantIds)
            AND i.deleted_at IS NULL
        GROUP BY i.variant_id
        """, nativeQuery = true)
    List<Object[]> findAvailableQuantitiesByVariantIds(@Param("variantIds") List<String> variantIds);
}