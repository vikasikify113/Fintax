package com.fintax.service;

import com.fintax.dto.ArticleDetailResponse;
import com.fintax.dto.ArticleRequest;
import com.fintax.dto.ArticleSummaryResponse;
import com.fintax.entity.Article;
import com.fintax.entity.Category;
import com.fintax.exception.ApiException;
import com.fintax.repository.ArticleRepository;
import com.fintax.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;

    public List<ArticleSummaryResponse> getByCategory(Long categoryId) {
        return articleRepository.findByCategoryId(categoryId).stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    public List<ArticleSummaryResponse> getAll() {
        return articleRepository.findAll().stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    public List<ArticleSummaryResponse> searchByTitle(String keyword) {
        return articleRepository.findByTitleContainingIgnoreCase(keyword).stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    @Transactional
    public ArticleDetailResponse getBySlug(String slug, String level) {
        Article article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ApiException("Article not found", HttpStatus.NOT_FOUND));

        article.setViewCount(article.getViewCount() + 1); // increment on read
        return toDetail(article, normalizeLevel(level));
    }

    public ArticleDetailResponse create(ArticleRequest request) {
        return createInternal(request, null);
    }

    public ArticleDetailResponse createByAdmin(ArticleRequest request, Long adminUserId) {
        return createInternal(request, adminUserId);
    }

    private ArticleDetailResponse createInternal(ArticleRequest request, Long adminUserId) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ApiException("Category not found", HttpStatus.NOT_FOUND));

        Article article = new Article();
        article.setTitle(request.getTitle());
        article.setSlug(request.getSlug());
        article.setCategory(category);
        article.setBeginnerContent(request.getBeginnerContent());
        article.setIntermediateContent(request.getIntermediateContent());
        article.setAdvancedContent(request.getAdvancedContent());
        article.setExamples(request.getExamples());
        article.setCreatedBy(adminUserId);

        Article saved = articleRepository.save(article);
        return toDetail(saved, "BEGINNER");
    }

    public ArticleDetailResponse update(Long id, ArticleRequest request) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ApiException("Article not found", HttpStatus.NOT_FOUND));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ApiException("Category not found", HttpStatus.NOT_FOUND));

        article.setTitle(request.getTitle());
        article.setSlug(request.getSlug());
        article.setCategory(category);
        article.setBeginnerContent(request.getBeginnerContent());
        article.setIntermediateContent(request.getIntermediateContent());
        article.setAdvancedContent(request.getAdvancedContent());
        article.setExamples(request.getExamples());

        return toDetail(articleRepository.save(article), "BEGINNER");
    }

    public void delete(Long id) {
        if (!articleRepository.existsById(id)) {
            throw new ApiException("Article not found", HttpStatus.NOT_FOUND);
        }
        articleRepository.deleteById(id);
    }

    private ArticleSummaryResponse toSummary(Article a) {
        return new ArticleSummaryResponse(
                a.getId(), a.getTitle(), a.getSlug(),
                a.getCategory() != null ? a.getCategory().getName() : null,
                a.getViewCount()
        );
    }

    private ArticleDetailResponse toDetail(Article a, String level) {
        String content = switch (level) {
            case "INTERMEDIATE" -> a.getIntermediateContent();
            case "ADVANCED" -> a.getAdvancedContent();
            default -> a.getBeginnerContent();
        };

        return new ArticleDetailResponse(
                a.getId(), a.getTitle(), a.getSlug(),
                a.getCategory() != null ? a.getCategory().getName() : null,
                level, content, a.getExamples(), a.getViewCount()
        );
    }

    private String normalizeLevel(String level) {
        if (level == null) return "BEGINNER";
        String upper = level.trim().toUpperCase();
        return switch (upper) {
            case "INTERMEDIATE", "ADVANCED" -> upper;
            default -> "BEGINNER";
        };
    }
}
