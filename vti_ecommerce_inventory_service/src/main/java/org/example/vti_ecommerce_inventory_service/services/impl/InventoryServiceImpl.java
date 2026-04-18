package org.example.vti_ecommerce_inventory_service.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.vti_ecommerce_inventory_service.dtos.requests.CheckAvailabilityRequest;
import org.example.vti_ecommerce_inventory_service.dtos.responses.CheckAvailabilityResponse;
import org.example.vti_ecommerce_inventory_service.repositories.InventoryRepository;
import org.example.vti_ecommerce_inventory_service.services.InventoryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String AVAILABILITY_CACHE_PREFIX = "inventory:availability:";

    @Value("${inventory.cache.availability-ttl-seconds}")
    private long availabilityTtlSeconds;

    // Api này không cần redis cache commit sau phải loại bỏ redis khỏi api
    @Override
    public CheckAvailabilityResponse checkAvailability(CheckAvailabilityRequest request) {

        List<String> variantIds = request.getItems().stream()
                .map(CheckAvailabilityRequest.AvailabilityItem::getVariantId)
                .toList();

        // Bulk query DB một lần duy nhất — tận dụng index idx_variant_id
        List<Object[]> dbResults = inventoryRepository
                .findAvailableQuantitiesByVariantIds(variantIds);

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
}