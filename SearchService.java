package com.fintax.service;

import com.fintax.dto.*;
import com.fintax.entity.CaProfile;
import com.fintax.entity.CaSpecialization;
import com.fintax.entity.Faq;
import com.fintax.entity.SearchHistory;
import com.fintax.entity.User;
import com.fintax.repository.CaProfileRepository;
import com.fintax.repository.FaqRepository;
import com.fintax.repository.SearchHistoryRepository;
import com.fintax.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final ArticleService articleService;
    private final FaqRepository faqRepository;
    private final CaProfileRepository caProfileRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final CurrentUserService currentUserService;

    public SearchResponse search(String query) {
        logSearch(query);

        List<ArticleSummaryResponse> articles = articleService.searchByTitle(query);

        List<FaqResponse> faqs = faqRepository.findByQuestionContainingIgnoreCase(query).stream()
                .map(this::toFaqResponse)
                .collect(Collectors.toList());

        List<CaProfileResponse> caProfiles = caProfileRepository
                .findByFullNameContainingIgnoreCaseOrCityContainingIgnoreCase(query, query).stream()
                .map(this::toCaResponse)
                .collect(Collectors.toList());

        return new SearchResponse(query, articles, faqs, caProfiles);
    }

    private void logSearch(String query) {
        try {
            SearchHistory history = new SearchHistory();
            history.setQuery(query);
            try {
                User user = currentUserService.getCurrentUser();
                history.setUser(user);
            } catch (Exception ignored) {
                // guest search — user stays null
            }
            searchHistoryRepository.save(history);
        } catch (Exception ignored) {
            // never let logging break the actual search
        }
    }

    private FaqResponse toFaqResponse(Faq f) {
        return new FaqResponse(
                f.getId(), f.getQuestion(), f.getAnswer(), f.getLevel().name(),
                f.getArticle() != null ? f.getArticle().getId() : null,
                f.getCategory() != null ? f.getCategory().getId() : null
        );
    }

    private CaProfileResponse toCaResponse(CaProfile p) {
        List<String> specNames = p.getSpecializations().stream()
                .map(CaSpecialization::getName)
                .collect(Collectors.toList());

        return new CaProfileResponse(
                p.getId(), p.getFullName(), p.getQualification(), p.getCity(), p.getState(),
                p.getPincode(), p.getExperienceYears(), p.getLanguages(), p.getBio(),
                p.getContactEmail(), p.getContactPhone(), p.getIsVerified(),
                p.getRatingAvg(), p.getRatingCount(), specNames
        );
    }
}
