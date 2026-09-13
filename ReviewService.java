package com.fintax.service;

import com.fintax.dto.ReviewRequest;
import com.fintax.dto.ReviewResponse;
import com.fintax.entity.CaProfile;
import com.fintax.entity.Review;
import com.fintax.entity.User;
import com.fintax.exception.ApiException;
import com.fintax.repository.CaProfileRepository;
import com.fintax.repository.ReviewRepository;
import com.fintax.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final CaProfileRepository caProfileRepository;
    private final CurrentUserService currentUserService;

    public List<ReviewResponse> getReviewsForCa(Long caProfileId) {
        return reviewRepository.findByCaProfileId(caProfileId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReviewResponse addReview(Long caProfileId, ReviewRequest request) {
        User user = currentUserService.getCurrentUser();

        if (reviewRepository.findByUserIdAndCaProfileId(user.getId(), caProfileId).isPresent()) {
            throw new ApiException("You have already reviewed this CA", HttpStatus.CONFLICT);
        }

        CaProfile caProfile = caProfileRepository.findById(caProfileId)
                .orElseThrow(() -> new ApiException("CA profile not found", HttpStatus.NOT_FOUND));

        Review review = new Review();
        review.setCaProfile(caProfile);
        review.setUser(user);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        reviewRepository.save(review);

        recalculateRating(caProfile);

        return toResponse(review);
    }

    private void recalculateRating(CaProfile caProfile) {
        List<Review> reviews = reviewRepository.findByCaProfileId(caProfile.getId());
        double average = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);

        caProfile.setRatingAvg(BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP));
        caProfile.setRatingCount(reviews.size());
        caProfileRepository.save(caProfile);
    }

    private ReviewResponse toResponse(Review r) {
        return new ReviewResponse(
                r.getId(), r.getUser().getFullName(), r.getRating(), r.getComment(), r.getCreatedAt()
        );
    }
}
