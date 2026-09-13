package com.fintax.controller;

import com.fintax.dto.FaqResponse;
import com.fintax.service.FaqService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faqs")
@RequiredArgsConstructor
public class FaqController {

    private final FaqService faqService;

    // GET /api/faqs?articleId=1  or  /api/faqs?categoryId=2  or /api/faqs (all)
    @GetMapping
    public ResponseEntity<List<FaqResponse>> getAll(
            @RequestParam(required = false) Long articleId,
            @RequestParam(required = false) Long categoryId) {

        if (articleId != null) return ResponseEntity.ok(faqService.getByArticle(articleId));
        if (categoryId != null) return ResponseEntity.ok(faqService.getByCategory(categoryId));
        return ResponseEntity.ok(faqService.getAll());
    }
}
