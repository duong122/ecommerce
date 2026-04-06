package org.example.vti_ecommerce_product_service.services.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.example.vti_ecommerce_product_service.dtos.requests.CreateProductRequest;
import org.example.vti_ecommerce_product_service.dtos.requests.CreateVariantRequest;
import org.example.vti_ecommerce_product_service.dtos.responses.ProductDetailResponse;
import org.example.vti_ecommerce_product_service.entities.Product;
import org.example.vti_ecommerce_product_service.entities.ProductImage;
import org.example.vti_ecommerce_product_service.entities.ProductVariant;
import org.example.vti_ecommerce_product_service.exceptions.DuplicateResourceException;
import org.example.vti_ecommerce_product_service.exceptions.ResourceNotFoundException;
import org.example.vti_ecommerce_product_service.repositories.CategoryRepository;
import org.example.vti_ecommerce_product_service.repositories.ProductImageRepository;
import org.example.vti_ecommerce_product_service.repositories.ProductRepository;
import org.example.vti_ecommerce_product_service.repositories.ProductVariantRepository;
import org.example.vti_ecommerce_product_service.services.AdminProductService;
import org.example.vti_ecommerce_product_service.utils.SlugUtil;
import org.springframework.data.redis.core.RedisTemplate;
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
    private static final String PRODUCT_LIST_CACHE_PREFIX = "products:list:";

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
            // Kafka producer sẽ implement ở bước sau khi setup Kafka
            log.info("TODO: publish product.created event for productId: {}", product.getId());
        } catch (Exception e) {
            log.warn("Failed to publish product.created event: {}", e.getMessage());
        }
    }
}
