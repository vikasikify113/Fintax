package com.fintax.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "search_history")
@Getter
@Setter
@NoArgsConstructor
public class SearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // nullable — guests can search too

    @Column(nullable = false, length = 300)
    private String query;

    @Column(name = "searched_at", updatable = false, insertable = false)
    private LocalDateTime searchedAt;
}
