package com.fintax.controller;

import com.fintax.dto.CategoryRequest;
import com.fintax.dto.CategoryResponse;
import com.fintax.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAll(
            @RequestParam(required = false, defaultValue = "false") boolean topLevelOnly) {
        return ResponseEntity.ok(topLevelOnly ? categoryService.getAllTopLevel() : categoryService.getAll());
    }

    @GetMapping("/{slug}")
    public ResponseEntity<CategoryResponse> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(categoryService.getBySlug(slug));
    }

    // Admin-only in practice — protect further once the Admin module/role checks are added
    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.create(request));
    }
}
