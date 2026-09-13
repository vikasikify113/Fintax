package com.fintax.repository;

import com.fintax.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    Optional<Article> findBySlug(String slug);
    List<Article> findByCategoryId(Long categoryId);
    List<Article> findByTitleContainingIgnoreCase(String keyword);
}
