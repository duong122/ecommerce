package org.example.vti_ecommerce_inventory_service.repositories;

import org.example.vti_ecommerce_inventory_service.entities.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, String> {
}