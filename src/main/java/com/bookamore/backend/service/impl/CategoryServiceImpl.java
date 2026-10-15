package com.bookamore.backend.service.impl;

import com.bookamore.backend.dto.category.CategoryResponse;
import com.bookamore.backend.exception.ResourceNotFoundException;
import com.bookamore.backend.service.CategoryService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final List<CategoryResponse> categories;
    private final List<String> categoryIds;
    private final Map<String, CategoryResponse> categoriesById;

    public CategoryServiceImpl(ObjectMapper objectMapper) throws IOException {
        try (InputStream input = new ClassPathResource("book-categories.json").getInputStream()) {
            categories = List.copyOf(objectMapper.readValue(input, new TypeReference<List<CategoryResponse>>() {}));
        }
        categoryIds = categories.stream().map(CategoryResponse::id).toList();
        Map<String, CategoryResponse> index = new HashMap<>();
        for (CategoryResponse category : categories) {
            index.putIfAbsent(category.id(), category);
        }
        categoriesById = Collections.unmodifiableMap(index);
    }

    @Override
    public List<String> getCategories() {
        return categoryIds;
    }

    @Override
    public CategoryResponse getCategoryById(String id) {
        return Optional.ofNullable(categoriesById.get(id))
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categories;
    }
}
