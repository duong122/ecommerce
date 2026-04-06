package org.example.vti_ecommerce_product_service.services.impl;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


import org.example.vti_ecommerce_product_service.dtos.requests.ProductFilterRequest;
import org.example.vti_ecommerce_product_service.dtos.responses.PagedResponse;
import org.example.vti_ecommerce_product_service.dtos.responses.ProductDetailResponse;
import org.example.vti_ecommerce_product_service.dtos.responses.ProductSummaryResponse;
import org.example.vti_ecommerce_product_service.exceptions.ResourceNotFoundException;
import org.example.vti_ecommerce_product_service.projections.ProductBaseProjection;
import org.example.vti_ecommerce_product_service.projections.ProductSummaryProjection;
import org.example.vti_ecommerce_product_service.repositories.CategoryRepository;
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
    private final ProductVariantRepository productVariantRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryRepository categoryRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final Integer CACHE_TTL_SECONDS = 300;
    private static final String PRODUCT_DETAIL_CACHE_PREFIX = "product:detail:";
    private static final String PRODUCT_LIST_CACHE_PREFIX = "products:list:";

    private static final List<String> VALID_SORT_OPTIONS = List.of("newest", "price_asc", "price_desc", "name_asc");

    @Override
    public PagedResponse<ProductSummaryResponse> getProducts(ProductFilterRequest request) {

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
            request.getSize());
    }

    @Override
    public ProductDetailResponse getProductById(String id) {
        String cacheKey = PRODUCT_DETAIL_CACHE_PREFIX + id;

        try {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return objectMapper.readValue(cached, ProductDetailResponse.class);
            }
        } catch (Exception e) {
            log.warn("Redis read failed, fallback to DB. Key: {}, Error: {}", cacheKey, e.getMessage());
        }

        ProductBaseProjection product = productRepository.findProductBaseById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        List<ProductDetailResponse.VariantResponse> variants = productVariantRepository
                .findVariantsByProductId(id)
                .stream()
                .map(v -> ProductDetailResponse.VariantResponse.builder()
                        .variantId(v.getVariantId())
                        .sku(v.getSku())
                        .price(v.getPrice())
                        .color(v.getColor())
                        .size(v.getSize())
                        .weight(v.getWeight())
                        .isActive(v.getVariantIsActive())
                        .build())
                .collect(Collectors.toList());

        List<ProductDetailResponse.ImageResponse> images = productImageRepository
                .findImagesByProductId(id)
                .stream()
                .map(i -> ProductDetailResponse.ImageResponse.builder()
                        .imageId(i.getImageId())
                        .variantId(i.getVariantId())
                        .url(i.getImageUrl())
                        .altText(i.getAltText())
                        .sortOrder(i.getSortOrder())
                        .isPrimary(i.getIsPrimary())
                        .build())
                .collect(Collectors.toList());

        ProductDetailResponse response = ProductDetailResponse.builder()
                .id(product.getId())
                .categoryId(product.getCategoryId())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .isActive(product.getIsActive())
                .variants(variants)
                .images(images)
                .build();

        try {
            String json = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(cacheKey, json, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Redis write failed. Key: {}, Error: {}", cacheKey, e.getMessage());
        }

        return response;
    }

}
