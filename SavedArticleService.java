package com.fintax.service;

import com.fintax.dto.ArticleSummaryResponse;
import com.fintax.entity.Article;
import com.fintax.entity.SavedArticle;
import com.fintax.entity.User;
import com.fintax.exception.ApiException;
import com.fintax.repository.ArticleRepository;
import com.fintax.repository.SavedArticleRepository;
import com.fintax.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SavedArticleService {

    private final SavedArticleRepository savedArticleRepository;
    private final ArticleRepository articleRepository;
    private final CurrentUserService currentUserService;

    public List<ArticleSummaryResponse> getSavedArticles() {
        User user = currentUserService.getCurrentUser();
        return savedArticleRepository.findByUserId(user.getId()).stream()
                .map(sa -> toSummary(sa.getArticle()))
                .collect(Collectors.toList());
    }

    public void saveArticle(Long articleId) {
        User user = currentUserService.getCurrentUser();

        if (savedArticleRepository.existsByUserIdAndArticleId(user.getId(), articleId)) {
            return; // already saved — idempotent
        }

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ApiException("Article not found", HttpStatus.NOT_FOUND));

        SavedArticle savedArticle = new SavedArticle();
        savedArticle.setUser(user);
        savedArticle.setArticle(article);
        savedArticleRepository.save(savedArticle);
    }

    public void unsaveArticle(Long articleId) {
        User user = currentUserService.getCurrentUser();
        savedArticleRepository.findByUserIdAndArticleId(user.getId(), articleId)
                .ifPresent(savedArticleRepository::delete);
    }

    private ArticleSummaryResponse toSummary(Article a) {
        return new ArticleSummaryResponse(
                a.getId(), a.getTitle(), a.getSlug(),
                a.getCategory() != null ? a.getCategory().getName() : null,
                a.getViewCount()
        );
    }
}
