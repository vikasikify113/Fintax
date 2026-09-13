package com.fintax.repository;

import com.fintax.entity.Faq;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FaqRepository extends JpaRepository<Faq, Long> {
    List<Faq> findByArticleId(Long articleId);
    List<Faq> findByCategoryId(Long categoryId);
    List<Faq> findByQuestionContainingIgnoreCase(String keyword);
}
