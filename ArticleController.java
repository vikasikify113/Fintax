package com.fintax.controller;

import com.fintax.dto.ArticleDetailResponse;
import com.fintax.dto.ArticleRequest;
import com.fintax.dto.ArticleSummaryResponse;
import com.fintax.service.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    // GET /api/articles?categoryId=2   or   /api/articles?search=itr
    @GetMapping
    public ResponseEntity<List<ArticleSummaryResponse>> getAll(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search) {

        if (categoryId != null) return ResponseEntity.ok(articleService.getByCategory(categoryId));
        if (search != null && !search.isBlank()) return ResponseEntity.ok(articleService.searchByTitle(search));
        return ResponseEntity.ok(articleService.getAll());
    }

    // GET /api/articles/what-is-tds?level=INTERMEDIATE
    @GetMapping("/{slug}")
    public ResponseEntity<ArticleDetailResponse> getBySlug(
            @PathVariable String slug,
            @RequestParam(required = false, defaultValue = "BEGINNER") String level) {
        return ResponseEntity.ok(articleService.getBySlug(slug, level));
    }

    // Admin-only in practice — protect further once the Admin module/role checks are added
    @PostMapping
    public ResponseEntity<ArticleDetailResponse> create(@Valid @RequestBody ArticleRequest request) {
        return ResponseEntity.ok(articleService.create(request));
    }
}
