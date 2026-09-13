package com.fintax.controller;

import com.fintax.dto.ArticleSummaryResponse;
import com.fintax.service.SavedArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/me/saved-articles")
@RequiredArgsConstructor
public class SavedArticleController {

    private final SavedArticleService savedArticleService;

    @GetMapping
    public ResponseEntity<List<ArticleSummaryResponse>> getSaved() {
        return ResponseEntity.ok(savedArticleService.getSavedArticles());
    }

    @PostMapping("/{articleId}")
    public ResponseEntity<Void> save(@PathVariable Long articleId) {
        savedArticleService.saveArticle(articleId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{articleId}")
    public ResponseEntity<Void> unsave(@PathVariable Long articleId) {
        savedArticleService.unsaveArticle(articleId);
        return ResponseEntity.noContent().build();
    }
}
