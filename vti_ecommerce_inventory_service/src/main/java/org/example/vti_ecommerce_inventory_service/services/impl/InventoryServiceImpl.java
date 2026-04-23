package org.example.vti_ecommerce_inventory_service.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.vti_ecommerce_inventory_service.dtos.requests.CheckAvailabilityRequest;
import org.example.vti_ecommerce_inventory_service.dtos.requests.ConfirmRequest;
import org.example.vti_ecommerce_inventory_service.dtos.requests.ReleaseRequest;
import org.example.vti_ecommerce_inventory_service.dtos.requests.ReserveRequest;
import org.example.vti_ecommerce_inventory_service.dtos.responses.CheckAvailabilityResponse;
import org.example.vti_ecommerce_inventory_service.dtos.responses.ConfirmResponse;
import org.example.vti_ecommerce_inventory_service.dtos.responses.ReleaseResponse;
import org.example.vti_ecommerce_inventory_service.dtos.responses.ReserveResponse;
import org.example.vti_ecommerce_inventory_service.entities.Inventory;
import org.example.vti_ecommerce_inventory_service.entities.InventoryTransaction;
import org.example.vti_ecommerce_inventory_service.exceptions.InventoryConfirmException;
import org.example.vti_ecommerce_inventory_service.exceptions.InventoryReleaseException;
import org.example.vti_ecommerce_inventory_service.exceptions.InventoryReservationException;
import org.example.vti_ecommerce_inventory_service.repositories.InventoryRepository;
import org.example.vti_ecommerce_inventory_service.repositories.InventoryTransactionRepository;
import org.example.vti_ecommerce_inventory_service.services.InventoryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final InventoryTransactionRepository transactionRepository;

    private static final String AVAILABILITY_CACHE_PREFIX = "inventory:availability:";

    @Value("${inventory.cache.availability-ttl-seconds}")
    private long availabilityTtlSeconds;

    @Override
    public CheckAvailabilityResponse checkAvailability(CheckAvailabilityRequest request) {

        List<String> variantIds = request.getItems().stream()
                .map(CheckAvailabilityRequest.AvailabilityItem::getVariantId)
                .toList();

        List<Object[]> dbResults = inventoryRepository.findAvailableQuantitiesByVariantIds(variantIds);

        // Build map variantId -> availableQuantity từ kết quả DB
        Map<String, Integer> availabilityMap = new HashMap<>();
        for (Object[] row : dbResults) {
            String variantId = (String) row[0];
            Integer availableQty = ((Number) row[1]).intValue();
            availabilityMap.put(variantId, availableQty);
        }

        // Build response — so sánh requested vs available
        List<CheckAvailabilityResponse.AvailabilityItemResult> itemResults = new ArrayList<>();
        boolean allAvailable = true;

        for (CheckAvailabilityRequest.AvailabilityItem item : request.getItems()) {
            Integer availableQty = availabilityMap.getOrDefault(item.getVariantId(), 0);
            boolean isAvailable = availableQty >= item.getQuantity();

            if (!isAvailable) {
                allAvailable = false;
            }

            itemResults.add(CheckAvailabilityResponse.AvailabilityItemResult.builder()
                    .variantId(item.getVariantId())
                    .requestedQuantity(item.getQuantity())
                    .availableQuantity(availableQty)
                    .isAvailable(isAvailable)
                    .build());
        }

        return CheckAvailabilityResponse.builder()
                .available(allAvailable)
                .items(itemResults)
                .build();
    }

    @Override
    @Transactional
    public ReserveResponse reserve(ReserveRequest request) {

        List<String> variantIds = request.getItems().stream()
                .map(ReserveRequest.ReserveItem::getVariantId)
                .toList();

        Map<String, Integer> requestedQtyMap = request.getItems().stream()
                .collect(Collectors.toMap(
                        ReserveRequest.ReserveItem::getVariantId,
                        ReserveRequest.ReserveItem::getQuantity));

        List<Inventory> inventories = inventoryRepository.findByVariantIdsForUpdate(variantIds);

        Map<String, Inventory> inventoryMap = inventories.stream()
                .collect(Collectors.toMap(Inventory::getVariantId, inv -> inv));

        List<ReserveResponse.ReserveItemResult> results = new ArrayList<>();
        boolean allSuccess = true;

        for (ReserveRequest.ReserveItem item : request.getItems()) {
            Inventory inv = inventoryMap.get(item.getVariantId());

            if (inv == null || inv.getAvailableQuantity() < item.getQuantity()) {
                allSuccess = false;
                results.add(ReserveResponse.ReserveItemResult.builder()
                        .variantId(item.getVariantId())
                        .requestedQuantity(item.getQuantity())
                        .reservedQuantity(0)
                        .success(false)
                        .failureReason(inv == null ? "Variant not found" : "Insufficient stock")
                        .build());
            } else {
                results.add(ReserveResponse.ReserveItemResult.builder()
                        .variantId(item.getVariantId())
                        .requestedQuantity(item.getQuantity())
                        .reservedQuantity(item.getQuantity())
                        .success(true)
                        .build());
            }
        }

        if (!allSuccess) {
            throw new InventoryReservationException("Reservation failed for some items", results);
        }

        List<InventoryTransaction> transactions = new ArrayList<>();

        for (Inventory inv : inventories) {
            Integer qty = requestedQtyMap.get(inv.getVariantId());
            inv.setReservedQuantity(inv.getReservedQuantity() + qty);
            inv.setAvailableQuantity(inv.getAvailableQuantity() - qty);

            transactions.add(InventoryTransaction.builder()
                    .inventoryId(inv.getId())
                    .variantId(inv.getVariantId())
                    .warehouseId(inv.getWarehouseId())
                    .transactionType(InventoryTransaction.TransactionType.reserve)
                    .quantity(qty)
                    .referenceType(InventoryTransaction.ReferenceType.order)
                    .referenceId(request.getOrderId())
                    .note("Reserved for order " + request.getOrderId())
                    .build());
        }

        transactionRepository.saveAll(transactions);

        return ReserveResponse.builder()
                .success(true)
                .orderId(request.getOrderId())
                .items(results)
                .build();
    }

    @Override
    @Transactional
    public ReleaseResponse release(ReleaseRequest request) {

        List<String> variantIds = request.getItems().stream()
                .map(ReleaseRequest.ReleaseItem::getVariantId)
                .toList();

        Map<String, Integer> requestedQtyMap = request.getItems().stream()
                .collect(Collectors.toMap(
                        ReleaseRequest.ReleaseItem::getVariantId,
                        ReleaseRequest.ReleaseItem::getQuantity));

        List<Inventory> inventories = inventoryRepository.findByVariantIdsForUpdate(variantIds);

        Map<String, Inventory> inventoryMap = inventories.stream()
                .collect(Collectors.toMap(Inventory::getVariantId, inv -> inv));

        List<ReleaseResponse.ReleaseItemResult> results = new ArrayList<>();
        boolean allSuccess = true;

        for (ReleaseRequest.ReleaseItem item : request.getItems()) {
            Inventory inv = inventoryMap.get(item.getVariantId());

            String failureReason = null;

            if (inv == null) {
                failureReason = "Variant not found";
            } else if (inv.getReservedQuantity() < item.getQuantity()) {
                // Không thể release nhiều hơn đã reserve
                failureReason = "Release quantity exceeds reserved quantity. Reserved: "
                        + inv.getReservedQuantity() + ", requested: " + item.getQuantity();
            }

            if (failureReason != null) {
                allSuccess = false;
                results.add(ReleaseResponse.ReleaseItemResult.builder()
                        .variantId(item.getVariantId())
                        .requestedQuantity(item.getQuantity())
                        .success(false)
                        .failureReason(failureReason)
                        .build());
            } else {
                results.add(ReleaseResponse.ReleaseItemResult.builder()
                        .variantId(item.getVariantId())
                        .requestedQuantity(item.getQuantity())
                        .success(true)
                        .build());
            }
        }

        if (!allSuccess) {
            throw new InventoryReleaseException("Release failed for some items", results);
        }

        List<InventoryTransaction> transactions = new ArrayList<>();

        for (Inventory inv : inventories) {
            Integer qty = requestedQtyMap.get(inv.getVariantId());
            inv.setReservedQuantity(inv.getReservedQuantity() - qty);
            inv.setAvailableQuantity(inv.getAvailableQuantity() + qty);

            transactions.add(InventoryTransaction.builder()
                    .inventoryId(inv.getId())
                    .variantId(inv.getVariantId())
                    .warehouseId(inv.getWarehouseId())
                    .transactionType(InventoryTransaction.TransactionType.release)
                    .quantity(qty)
                    .referenceType(InventoryTransaction.ReferenceType.order)
                    .referenceId(request.getOrderId())
                    .note("Released for order " + request.getOrderId())
                    .build());
        }

        transactionRepository.saveAll(transactions);

        return ReleaseResponse.builder()
                .success(true)
                .orderId(request.getOrderId())
                .items(results)
                .build();
    }

    @Override
    @Transactional
    public ConfirmResponse confirm(ConfirmRequest request) {

        List<String> variantIds = request.getItems().stream()
                .map(ConfirmRequest.ConfirmItem::getVariantId)
                .toList();

        Map<String, Integer> requestedQtyMap = request.getItems().stream()
                .collect(Collectors.toMap(
                        ConfirmRequest.ConfirmItem::getVariantId,
                        ConfirmRequest.ConfirmItem::getQuantity));

        List<Inventory> inventories = inventoryRepository.findByVariantIdsForUpdate(variantIds);

        Map<String, Inventory> inventoryMap = inventories.stream()
                .collect(Collectors.toMap(Inventory::getVariantId, inv -> inv));

        List<ConfirmResponse.ConfirmItemResult> results = new ArrayList<>();
        boolean allSuccess = true;

        for (ConfirmRequest.ConfirmItem item : request.getItems()) {
            Inventory inv = inventoryMap.get(item.getVariantId());

            String failureReason = null;

            if (inv == null) {
                failureReason = "Variant not found";
            } else if (inv.getReservedQuantity() < item.getQuantity()) {
                failureReason = "Confirm quantity exceeds reserved quantity. Reserved: "
                        + inv.getReservedQuantity() + ", requested: " + item.getQuantity();
            }

            if (failureReason != null) {
                allSuccess = false;
                results.add(ConfirmResponse.ConfirmItemResult.builder()
                        .variantId(item.getVariantId())
                        .requestedQuantity(item.getQuantity())
                        .success(false)
                        .failureReason(failureReason)
                        .build());
            } else {
                results.add(ConfirmResponse.ConfirmItemResult.builder()
                        .variantId(item.getVariantId())
                        .requestedQuantity(item.getQuantity())
                        .success(true)
                        .build());
            }
        }

        if (!allSuccess) {
            throw new InventoryConfirmException("Confirm failed for some items", results);
        }

        List<InventoryTransaction> transactions = new ArrayList<>();

        for (Inventory inv : inventories) {
            Integer qty = requestedQtyMap.get(inv.getVariantId());

            inv.setTotalQuantity(inv.getTotalQuantity() - qty);
            inv.setReservedQuantity(inv.getReservedQuantity() - qty);

            transactions.add(InventoryTransaction.builder()
                    .inventoryId(inv.getId())
                    .variantId(inv.getVariantId())
                    .warehouseId(inv.getWarehouseId())
                    .transactionType(InventoryTransaction.TransactionType.export)
                    .quantity(qty)
                    .referenceType(InventoryTransaction.ReferenceType.order)
                    .referenceId(request.getOrderId())
                    .note("Confirmed export for order " + request.getOrderId())
                    .build());
        }

        transactionRepository.saveAll(transactions);

        return ConfirmResponse.builder()
                .success(true)
                .orderId(request.getOrderId())
                .items(results)
                .build();
    }
}