package org.example.vti_ecommerce_product_service.services.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.example.vti_ecommerce_product_service.dtos.responses.CategoryTreeResponse;
import org.example.vti_ecommerce_product_service.projections.CategoryProjection;
import org.example.vti_ecommerce_product_service.repositories.CategoryRepository;
import org.example.vti_ecommerce_product_service.services.CategoryService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

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
    
}
