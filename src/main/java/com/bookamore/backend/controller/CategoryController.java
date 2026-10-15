package com.bookamore.backend.controller;

import com.bookamore.backend.annotation.No400Swgr;
import com.bookamore.backend.annotation.No401Swgr;
import com.bookamore.backend.annotation.No404Swgr;
import com.bookamore.backend.dto.category.CategoryResponse;
import com.bookamore.backend.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Get supported book category IDs")
    @No400Swgr
    @No401Swgr
    @No404Swgr
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> getCategories() {
        return categoryService.getCategories();
    }

    @Operation(summary = "Get book category by ID",
            description = "Returns the category ID with English and Ukrainian names.")
    @No400Swgr
    @No401Swgr
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable("id") String id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @Operation(summary = "Get all supported book categories",
            description = "Returns category IDs with English and Ukrainian names.")
    @No400Swgr
    @No401Swgr
    @No404Swgr
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CategoryResponse> getAllCategories() {
        return categoryService.getAllCategories();
    }
}
