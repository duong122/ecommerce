package org.example.vti_ecommerce_product_service.services.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.kafka.common.errors.DuplicateResourceException;
import org.example.vti_ecommerce_product_service.dtos.requests.CreateCategoryRequest;
import org.example.vti_ecommerce_product_service.dtos.responses.CategoryResponse;
import org.example.vti_ecommerce_product_service.dtos.responses.CategoryTreeResponse;
import org.example.vti_ecommerce_product_service.entities.Category;
import org.example.vti_ecommerce_product_service.exceptions.ResourceNotFoundException;
import org.example.vti_ecommerce_product_service.mappers.CategoryMapper;
import org.example.vti_ecommerce_product_service.projections.CategoryProjection;
import org.example.vti_ecommerce_product_service.repositories.CategoryRepository;
import org.example.vti_ecommerce_product_service.services.CategoryService;
import org.example.vti_ecommerce_product_service.utils.SlugUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService{

    private final CategoryRepository categoryRepository;
    private static final String CATEGORIES_CACHE_KEY = "categories:all";
    private final ObjectMapper objectMapper;

    private static final Integer CATEGORIES_CACHE_TTL = 600;
    private RedisTemplate<String, String> redisTemplate;

    private final CategoryMapper categoryMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String CATEGORY_LIST_CACHE_PREFIX = "categories:list:";
    private static final int MAX_CATEGORY_DEPTH = 3;

    @Value("${kafka.topics.category-created}")
    private String categoryCreatedTopic;

    @Override
    public List<CategoryTreeResponse> getAllCategoy() {

        try {
            String cached = redisTemplate.opsForValue().get(CATEGORIES_CACHE_KEY);
            if (cached != null) {
                return objectMapper.readValue(cached, objectMapper.getTypeFactory().constructCollectionType(
                    List.class, CategoryTreeResponse.class));
            }
        } catch (Exception ex) {
            log.warn("Redis read fail, fallback to DB. Error {}", ex.getMessage());
        }

        List<CategoryProjection> allCategories = categoryRepository.findAllActiveCategories();

        if (allCategories.isEmpty()) {
            return List.of();
        }

        Set<String> parentIds = allCategories.stream().map(CategoryProjection::getParentId)
                                                .filter(Objects::nonNull)
                                                .collect(Collectors.toSet());
        
        Map<String, CategoryTreeResponse> nodeMap = new LinkedHashMap<>();
        for (CategoryProjection c : allCategories) {
            Boolean isLeaf = !parentIds.contains(c.getId());

            CategoryTreeResponse node = CategoryTreeResponse.builder()
                                            .id(c.getId())
                                            .name(c.getName())
                                            .slug(c.getSlug())
                                            .sortOrder(c.getSortOrder())
                                            .thumbnailUrl(isLeaf ? c.getImgUrl() : null)
                                            .children(new ArrayList<>())
                                            .build();          
            nodeMap.put(c.getId(), node);                              
        }
        
        List<CategoryTreeResponse> roots = new ArrayList<>();
        for (CategoryProjection c : allCategories) {
            CategoryTreeResponse node = nodeMap.get(c.getId());
            if (c.getParentId() == null) {
                roots.add(node);
            } else {
                CategoryTreeResponse parent = nodeMap.get(c.getParentId());
                if (parent != null) {
                    parent.getChildren().add(node);
                }
            }
        }

        try {
            String json = objectMapper.writeValueAsString(roots);
            redisTemplate.opsForValue().set(CATEGORIES_CACHE_KEY, json, CATEGORIES_CACHE_TTL, TimeUnit.SECONDS);
        } catch(Exception exception) {
            log.warn("Redis writed failed Error: {}", exception.getMessage());
        }

        return roots;
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {



        if (categoryRepository.existsByNameAndDeletedAtIsNull(request.getName())) {
            throw new DuplicateResourceException("Category name already exists: " + request.getName());
        }

        if (request.getParentId() != null) {
            categoryRepository.findByIdAndDeletedAtIsNull(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Parent category not found with id: " + request.getParentId()));

            Integer parentDepth = categoryRepository.calculateDepthByParentId(request.getParentId());
            if (parentDepth != null && parentDepth >= MAX_CATEGORY_DEPTH) {
                throw new ResponseStatusException(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "Cannot create category: maximum depth of " + MAX_CATEGORY_DEPTH + " levels exceeded");
            }
        }

        String slug = generateUniqueSlug(request.getName());

        Category category = new Category();
        category.setName(request.getName());
        category.setSlug(slug);
        category.setParentId(request.getParentId());
        category.setDescription(request.getDescription());
        category.setImageUrl(request.getImageUrl());
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        category.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        Category savedCategory = categoryRepository.save(category);

        invalidateCategoryCache();

        publishCategoryCreatedEvent(savedCategory);

        return categoryMapper.toResponse(savedCategory);
    }

    private String generateUniqueSlug(String name) {
        String baseSlug = SlugUtil.toSlug(name);
        String slug = baseSlug;
        if (categoryRepository.existsBySlugAndDeletedAtIsNull(slug)) {
            slug = baseSlug + "-" + UUID.randomUUID().toString().substring(0, 6);
        }
        return slug;
    }

    private void invalidateCategoryCache() {
        try {
            Set<String> keys = redisTemplate.keys(CATEGORY_LIST_CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("Failed to invalidate category cache: {}", e.getMessage());
        }
    }

    private void publishCategoryCreatedEvent(Category category) {
        try {
            kafkaTemplate.send(categoryCreatedTopic, category.getId(),
                    categoryMapper.toCreatedEvent(category));
        } catch (Exception e) {
            log.warn("Failed to publish category.created event for categoryId: {}. Error: {}",
                    category.getId(), e.getMessage());
        }
    }
}
    
