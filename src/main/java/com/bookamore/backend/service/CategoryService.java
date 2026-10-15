package com.bookamore.backend.service;

import com.bookamore.backend.dto.category.CategoryResponse;

import java.util.List;

public interface CategoryService {

    List<String> getCategories();

    CategoryResponse getCategoryById(String id);

    List<CategoryResponse> getAllCategories();
}
