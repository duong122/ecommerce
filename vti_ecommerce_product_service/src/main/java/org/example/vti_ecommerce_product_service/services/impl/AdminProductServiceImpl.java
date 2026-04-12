package org.example.vti_ecommerce_product_service.services.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.example.vti_ecommerce_product_service.dtos.requests.CreateProductRequest;
import org.example.vti_ecommerce_product_service.dtos.requests.CreateVariantRequest;
import org.example.vti_ecommerce_product_service.dtos.requests.UpdateProductRequest;
import org.example.vti_ecommerce_product_service.dtos.responses.ProductDetailResponse;
import org.example.vti_ecommerce_product_service.entities.Product;
import org.example.vti_ecommerce_product_service.entities.ProductImage;
import org.example.vti_ecommerce_product_service.entities.ProductVariant;
import org.example.vti_ecommerce_product_service.exceptions.DuplicateResourceException;
import org.example.vti_ecommerce_product_service.exceptions.ResourceNotFoundException;
import org.example.vti_ecommerce_product_service.mappers.ProductMapper;
import org.example.vti_ecommerce_product_service.repositories.CategoryRepository;
import org.example.vti_ecommerce_product_service.repositories.ProductImageRepository;
import org.example.vti_ecommerce_product_service.repositories.ProductRepository;
import org.example.vti_ecommerce_product_service.repositories.ProductVariantRepository;
import org.example.vti_ecommerce_product_service.services.AdminProductService;
import org.example.vti_ecommerce_product_service.utils.SlugUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminProductServiceImpl implements AdminProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryRepository categoryRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ProductMapper productMapper;

    private static final String PRODUCT_LIST_CACHE_PREFIX = "products:list:";
    private static final String PRODUCT_CACHE_PREFIX = "product:";
    private static final String PRODUCT_SLUG_CACHE_PREFIX = "product:slug:";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.product-created}")
    private String productCreatedTopic;

    @Value("${kafka.topics.product-updated}")
    private String productUpdatedTopic;

    @Override
    @Transactional
    public ProductDetailResponse createProduct(CreateProductRequest request) {

        if (!categoryRepository.existsActiveById(request.getCategoryId())) {
            throw new ResourceNotFoundException("Category not found with id: " + request.getCategoryId());
        }

        if (productRepository.existsByNameAndDeletedAtIsNull(request.getName())) {
            throw new DuplicateResourceException("Product name already exists: " + request.getName());
        }

        List<String> skus = request.getVariants().stream()
                .map(CreateVariantRequest::getSku)
                .collect(Collectors.toList());

        Set<String> uniqueSkus = new HashSet<>(skus);
        if (uniqueSkus.size() != skus.size()) {
            throw new DuplicateResourceException("Duplicate SKU found within request");
        }

        for (String sku : skus) {
            if (productVariantRepository.existsBySkuAndDeletedAtIsNull(sku)) {
                throw new DuplicateResourceException("SKU already exists: " + sku);
            }
        }

        String slug = generateUniqueSlug(request.getName());

        Product product = new Product();
        product.setName(request.getName());
        product.setCategoryId(request.getCategoryId());
        product.setDescription(request.getDescription());
        product.setSlug(slug);
        product.setIsActive(true);
        Product savedProduct = productRepository.save(product);

        List<ProductVariant> variantsToSave = new ArrayList<>();
        for (CreateVariantRequest variantRequest : request.getVariants()) {
            ProductVariant variant = new ProductVariant();
            variant.setProductId(savedProduct.getId());
            variant.setSku(variantRequest.getSku());
            variant.setPrice(variantRequest.getPrice());
            variant.setColor(variantRequest.getColor());
            variant.setSize(variantRequest.getSize());
            variant.setWeight(variantRequest.getWeight());
            variant.setIsActive(true);
            variantsToSave.add(variant);
        }

        List<ProductVariant> savedVariants = productVariantRepository.saveAll(variantsToSave);

        List<ProductImage> imagesToSave = new ArrayList<>();
        for (int i = 0; i < savedVariants.size(); i++) {
            ProductVariant savedVariant = savedVariants.get(i);
            CreateVariantRequest variantRequest = request.getVariants().get(i);

            if (variantRequest.getImageUrls() != null && !variantRequest.getImageUrls().isEmpty()) {
                List<String> urls = variantRequest.getImageUrls();
                for (int j = 0; j < urls.size(); j++) {
                    ProductImage image = new ProductImage();
                    image.setProductId(savedProduct.getId());
                    image.setVariantId(savedVariant.getId());
                    image.setUrl(urls.get(j));
                    image.setSortOrder(j);
                    image.setIsPrimary(j == 0);
                    imagesToSave.add(image);
                }
            }
        }

        List<ProductImage> savedImages = productImageRepository.saveAll(imagesToSave);

        List<ProductDetailResponse.VariantResponse> variantResponseAfterSaved = savedVariants.stream()
                .map(v -> ProductDetailResponse.VariantResponse.builder()
                        .variantId(v.getId())
                        .sku(v.getSku())
                        .price(v.getPrice())
                        .color(v.getColor())
                        .size(v.getSize())
                        .weight(v.getWeight())
                        .isActive(v.getIsActive())
                        .build())
                .collect(Collectors.toList());

        List<ProductDetailResponse.ImageResponse> imageResponses = savedImages.stream()
                .map(img -> ProductDetailResponse.ImageResponse.builder()
                        .imageId(img.getId())
                        .variantId(img.getVariantId())
                        .url(img.getUrl())
                        .altText(img.getAltText())
                        .sortOrder(img.getSortOrder())
                        .isPrimary(img.getIsPrimary())
                        .build())
                .collect(Collectors.toList());

        invalidateProductListCache();

        publishProductCreatedEvent(savedProduct);

        return ProductDetailResponse.builder()
                .id(savedProduct.getId())
                .categoryId(savedProduct.getCategoryId())
                .name(savedProduct.getName())
                .slug(savedProduct.getSlug())
                .description(savedProduct.getDescription())
                .isActive(savedProduct.getIsActive())
                .variants(variantResponseAfterSaved)
                .images(imageResponses)
                .build();
    }

    private String generateUniqueSlug(String name) {
        String baseSlug = SlugUtil.toSlug(name);
        String slug = baseSlug;
        if (productRepository.existsBySlugAndDeletedAtIsNull(slug)) {
            slug = baseSlug + "-" + UUID.randomUUID().toString().substring(0, 6);
        }
        return slug;
    }

    private void invalidateProductListCache() {
        try {
            Set<String> keys = redisTemplate.keys(PRODUCT_LIST_CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("Failed to invalidate product list cache: {}", e.getMessage());
        }
    }

    private void publishProductCreatedEvent(Product product) {
        try {
            kafkaTemplate.send(productCreatedTopic, product.getId(), productMapper.toCreatedEvent(product));
        } catch (Exception e) {
            log.warn("Failed to publish product.created event for productId: {}. Error: {}",
                    product.getId(), e.getMessage());
        }
    }

    @Override
    @Transactional
    public ProductDetailResponse updateProduct(String id, UpdateProductRequest request) {

        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        if (request.getCategoryId() != null) {
            if (!categoryRepository.existsActiveById(request.getCategoryId())) {
                throw new ResourceNotFoundException("Category not found with id: " + request.getCategoryId());
            }
        }

        if (request.getName() != null && !request.getName().equals(product.getName())) {
            if (productRepository.existsByNameAndDeletedAtIsNullAndIdNot(request.getName(), id)) {
                throw new DuplicateResourceException("Product name already exists: " + request.getName());
            }
        }

        String oldSlug = product.getSlug();

        if (request.getName() != null && !request.getName().equals(product.getName())) {
            String newSlug = generateUniqueSlug(request.getName());
            product.setSlug(newSlug);
        }

        productMapper.updateProductFromRequest(request, product);

        Product savedProduct = productRepository.save(product);

        invalidateProductCache(savedProduct.getId(), oldSlug, savedProduct.getSlug());

        publishProductUpdatedEvent(savedProduct);

        return productMapper.toDetailResponse(savedProduct);
    }


    private void invalidateProductCache(String productId, String oldSlug, String newSlug) {
        try {
            redisTemplate.delete(PRODUCT_CACHE_PREFIX + productId);
            redisTemplate.delete(PRODUCT_SLUG_CACHE_PREFIX + oldSlug);
            if (!oldSlug.equals(newSlug)) {
                redisTemplate.delete(PRODUCT_SLUG_CACHE_PREFIX + newSlug);
            }
            invalidateProductListCache();
            log.info("Cache invalidated for product: {}", productId);
        } catch (Exception e) {
            log.warn("Failed to invalidate product cache for id: {}. Error: {}", productId, e.getMessage());
        }
    }

    private void publishProductUpdatedEvent(Product product) {
        try {
            kafkaTemplate.send(productUpdatedTopic, product.getId(), productMapper.toUpdatedEvent(product));
        } catch (Exception e) {
            log.warn("Failed to publish product.updated event for productId: {}. Error: {}",
                    product.getId(), e.getMessage());
        }
    }

}
