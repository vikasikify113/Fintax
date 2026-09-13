package com.fintax.repository;

import com.fintax.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findBySlug(String slug);
    List<Category> findByParentIsNull(); // top-level categories
    List<Category> findByParentId(Long parentId);
}
