package com.fintax.repository;

import com.fintax.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    @Query("""
        SELECT s.query AS query, COUNT(s) AS total
        FROM SearchHistory s
        GROUP BY s.query
        ORDER BY total DESC
        """)
    List<QueryCount> findTopQueries();

    interface QueryCount {
        String getQuery();
        Long getTotal();
    }
}
