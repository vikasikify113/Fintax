package com.fintax.controller;

import com.fintax.dto.ContactRequestDto;
import com.fintax.dto.ReviewRequest;
import com.fintax.dto.ReviewResponse;
import com.fintax.service.ContactRequestService;
import com.fintax.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ca/{caProfileId}")
@RequiredArgsConstructor
public class CaEngagementController {

    private final ReviewService reviewService;
    private final ContactRequestService contactRequestService;

    @GetMapping("/reviews")
    public ResponseEntity<List<ReviewResponse>> getReviews(@PathVariable Long caProfileId) {
        return ResponseEntity.ok(reviewService.getReviewsForCa(caProfileId));
    }

    // Requires authentication — reviewer identity comes from the logged-in user
    @PostMapping("/reviews")
    public ResponseEntity<ReviewResponse> addReview(@PathVariable Long caProfileId,
                                                     @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(reviewService.addReview(caProfileId, request));
    }

    // Public — guests can request a consultation too
    @PostMapping("/contact-requests")
    public ResponseEntity<Void> requestConsultation(@PathVariable Long caProfileId,
                                                      @Valid @RequestBody ContactRequestDto dto) {
        contactRequestService.createRequest(caProfileId, dto);
        return ResponseEntity.ok().build();
    }
}
