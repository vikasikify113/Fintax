package com.fintax.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "faqs")
@Getter
@Setter
@NoArgsConstructor
public class Faq {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "article_id")
    private Article article; // nullable

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category; // nullable

    @Column(nullable = false, length = 300)
    private String question;

    @Lob
    @Column(nullable = false)
    private String answer;

    @Enumerated(EnumType.STRING)
    private Level level = Level.BEGINNER;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    public enum Level {
        BEGINNER, INTERMEDIATE, ADVANCED
    }
}
