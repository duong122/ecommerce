package org.example.vti_ecommerce_product_service.services.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.example.vti_ecommerce_product_service.dtos.requests.ProductFilterRequest;
import org.example.vti_ecommerce_product_service.dtos.responses.BaseResponse;
import org.example.vti_ecommerce_product_service.dtos.responses.PagedResponse;
import org.example.vti_ecommerce_product_service.dtos.responses.ProductSummaryResponse;
import org.example.vti_ecommerce_product_service.entities.Product;
import org.example.vti_ecommerce_product_service.entities.ProductVariant;
import org.example.vti_ecommerce_product_service.mappers.ProductMapper;
import org.example.vti_ecommerce_product_service.projections.ProductSummaryProjection;
import org.example.vti_ecommerce_product_service.repositories.ProductImageRepository;
import org.example.vti_ecommerce_product_service.repositories.ProductRepository;
import org.example.vti_ecommerce_product_service.repositories.ProductVariantRepository;
import org.example.vti_ecommerce_product_service.services.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductVariantRepository productVariantRepository;
    private final ProductImageRepository productImageRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final Integer CACHE_TTL_SECONDS = 300;

    private static final List<String> VALID_SORT_OPTIONS = List.of("newest", "price_asc", "price_desc", "name_asc");

    @Override
    public PagedResponse<ProductSummaryResponse> getProducts(ProductFilterRequest request) {

        log.info(request.toString());

        normalizeRequest(request);

        String cacheKey = buildCacheKey(request);

        try {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return objectMapper.readValue(
                        cached,
                        objectMapper.getTypeFactory().constructParametricType(
                                PagedResponse.class, ProductSummaryResponse.class));
            }
        } catch (Exception e) {
            log.warn("Redis read failed, fallback to DB. Key: {}, Error: {}", cacheKey, e.getMessage());
        }

        Sort sort = buildSort(request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Page<ProductSummaryProjection> projectionPage = productRepository.findProductsWithFilter(
                request.getCategoryId(),
                request.getMinPrice(),
                request.getMaxPrice(),
                pageable);

        List<String> productIds = projectionPage.getContent()
                .stream()
                .map(ProductSummaryProjection::getId)
                .collect(Collectors.toList());

        Map<String, String> primaryImageMap = new HashMap<>();
        if (!productIds.isEmpty()) {
            productImageRepository.findPrimaryImagesByProductIds(productIds)
                    .forEach(img -> primaryImageMap.put(img.getProductId(), img.getUrl()));
        }

        Page<ProductSummaryResponse> responsePage = projectionPage.map(projection -> ProductSummaryResponse.builder()
                .id(projection.getId())
                .name(projection.getName())
                .slug(projection.getSlug())
                .categoryId(projection.getCategoryId())
                .primaryImageUrl(primaryImageMap.get(projection.getId()))
                .minPrice(projection.getMinPrice())
                .maxPrice(projection.getMaxPrice())
                .variantCount(projection.getVariantCount())
                .build());

        PagedResponse<ProductSummaryResponse> result = PagedResponse.of(responsePage);

        try {
            String json = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(cacheKey, json, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Redis write failed. Key: {}, Error: {}", cacheKey, e.getMessage());
        }

        return result;
    }

    private void normalizeRequest(ProductFilterRequest request) {
        if (!VALID_SORT_OPTIONS.contains(request.getSortBy())) {
            request.setSortBy("newest");
        }
        if (request.getSize() > 100) {
            request.setSize(100);
        }
        if (request.getMinPrice() != null && request.getMaxPrice() != null
                && request.getMinPrice() > request.getMaxPrice()) {
            throw new IllegalArgumentException("min_price không được lớn hơn max_price");
        }
    }

    private Sort buildSort(String sortBy) {
        return switch (sortBy) {
            case "price_asc" -> Sort.by("minPrice").ascending();
            case "price_desc" -> Sort.by("minPrice").descending();
            case "name_asc" -> Sort.by("name").ascending();
            default -> Sort.by("createdDate").descending();
        };
    }

    private String buildCacheKey(ProductFilterRequest request) {
        return String.format("products:list:%s:%s:%s:%s:%d:%d",
            request.getCategoryId(),
            request.getMinPrice(),
            request.getMaxPrice(),
            request.getSortBy(),
            request.getPage(),
            request.getSize()
        );
    }
}
