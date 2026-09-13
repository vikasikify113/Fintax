package com.fintax.service;

import com.fintax.dto.FaqRequest;
import com.fintax.dto.FaqResponse;
import com.fintax.entity.Article;
import com.fintax.entity.Category;
import com.fintax.entity.Faq;
import com.fintax.exception.ApiException;
import com.fintax.repository.ArticleRepository;
import com.fintax.repository.CategoryRepository;
import com.fintax.repository.FaqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FaqService {

    private final FaqRepository faqRepository;
    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;

    public List<FaqResponse> getByArticle(Long articleId) {
        return faqRepository.findByArticleId(articleId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<FaqResponse> getByCategory(Long categoryId) {
        return faqRepository.findByCategoryId(categoryId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<FaqResponse> getAll() {
        return faqRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public FaqResponse create(FaqRequest request) {
        Faq faq = new Faq();
        applyRequest(faq, request);
        return toResponse(faqRepository.save(faq));
    }

    public FaqResponse update(Long id, FaqRequest request) {
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new ApiException("FAQ not found", HttpStatus.NOT_FOUND));
        applyRequest(faq, request);
        return toResponse(faqRepository.save(faq));
    }

    public void delete(Long id) {
        if (!faqRepository.existsById(id)) {
            throw new ApiException("FAQ not found", HttpStatus.NOT_FOUND);
        }
        faqRepository.deleteById(id);
    }

    private void applyRequest(Faq faq, FaqRequest request) {
        faq.setQuestion(request.getQuestion());
        faq.setAnswer(request.getAnswer());
        faq.setLevel(Faq.Level.valueOf(request.getLevel() == null ? "BEGINNER" : request.getLevel().toUpperCase()));

        if (request.getArticleId() != null) {
            Article article = articleRepository.findById(request.getArticleId())
                    .orElseThrow(() -> new ApiException("Article not found", HttpStatus.NOT_FOUND));
            faq.setArticle(article);
        } else {
            faq.setArticle(null);
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ApiException("Category not found", HttpStatus.NOT_FOUND));
            faq.setCategory(category);
        } else {
            faq.setCategory(null);
        }
    }

    private FaqResponse toResponse(Faq f) {
        return new FaqResponse(
                f.getId(), f.getQuestion(), f.getAnswer(), f.getLevel().name(),
                f.getArticle() != null ? f.getArticle().getId() : null,
                f.getCategory() != null ? f.getCategory().getId() : null
        );
    }
}
