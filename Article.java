package com.fintax.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "articles")
@Getter
@Setter
@NoArgsConstructor
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, unique = true, length = 220)
    private String slug;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Lob
    @Column(name = "beginner_content")
    private String beginnerContent;

    @Lob
    @Column(name = "intermediate_content")
    private String intermediateContent;

    @Lob
    @Column(name = "advanced_content")
    private String advancedContent;

    @Lob
    private String examples;

    @Column(name = "view_count")
    private Long viewCount = 0L;

    // created_by references admin_users.id — kept as plain Long to avoid
    // pulling in the full AdminUser entity before that module is built.
    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false)
    private LocalDateTime updatedAt;
}
