package com.fintax.controller;

import com.fintax.dto.*;
import com.fintax.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ArticleService articleService;
    private final CategoryService categoryService;
    private final FaqService faqService;
    private final CaProfileService caProfileService;
    private final AdminService adminService;

    // ---------- Articles ----------
    @PutMapping("/articles/{id}")
    public ResponseEntity<ArticleDetailResponse> updateArticle(@PathVariable Long id, @Valid @RequestBody ArticleRequest request) {
        return ResponseEntity.ok(articleService.update(id, request));
    }

    @DeleteMapping("/articles/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        articleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- Categories ----------
    @PutMapping("/categories/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.update(id, request));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- FAQs ----------
    @PostMapping("/faqs")
    public ResponseEntity<FaqResponse> createFaq(@Valid @RequestBody FaqRequest request) {
        return ResponseEntity.ok(faqService.create(request));
    }

    @PutMapping("/faqs/{id}")
    public ResponseEntity<FaqResponse> updateFaq(@PathVariable Long id, @Valid @RequestBody FaqRequest request) {
        return ResponseEntity.ok(faqService.update(id, request));
    }

    @DeleteMapping("/faqs/{id}")
    public ResponseEntity<Void> deleteFaq(@PathVariable Long id) {
        faqService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- CA Profiles ----------
    @PostMapping("/ca")
    public ResponseEntity<CaProfileResponse> createCaProfile(@Valid @RequestBody CaProfileRequest request) {
        return ResponseEntity.ok(caProfileService.create(request));
    }

    @PutMapping("/ca/{id}")
    public ResponseEntity<CaProfileResponse> updateCaProfile(@PathVariable Long id, @Valid @RequestBody CaProfileRequest request) {
        return ResponseEntity.ok(caProfileService.update(id, request));
    }

    @PatchMapping("/ca/{id}/verify")
    public ResponseEntity<CaProfileResponse> verifyCaProfile(@PathVariable Long id, @RequestParam boolean verified) {
        return ResponseEntity.ok(caProfileService.setVerified(id, verified));
    }

    @DeleteMapping("/ca/{id}")
    public ResponseEntity<Void> deleteCaProfile(@PathVariable Long id) {
        caProfileService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- Users ----------
    @GetMapping("/users")
    public ResponseEntity<List<UserSummaryResponse>> getUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PatchMapping("/users/{id}/active")
    public ResponseEntity<Void> setUserActive(@PathVariable Long id, @RequestParam boolean active) {
        adminService.setUserActive(id, active);
        return ResponseEntity.ok().build();
    }

    // ---------- Reviews ----------
    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        adminService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- Contact Requests ----------
    @GetMapping("/contact-requests")
    public ResponseEntity<List<ContactRequestResponse>> getContactRequests() {
        return ResponseEntity.ok(adminService.getAllContactRequests());
    }

    @PatchMapping("/contact-requests/{id}/status")
    public ResponseEntity<ContactRequestResponse> updateContactRequestStatus(
            @PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(adminService.updateContactRequestStatus(id, status));
    }

    // ---------- Notifications ----------
    @PostMapping("/notifications")
    public ResponseEntity<Void> sendNotification(@Valid @RequestBody NotificationCreateRequest request) {
        adminService.sendNotification(request);
        return ResponseEntity.ok().build();
    }

    // ---------- Search Statistics ----------
    @GetMapping("/stats/search")
    public ResponseEntity<List<Map<String, Object>>> getSearchStats() {
        return ResponseEntity.ok(adminService.getTopSearchQueries());
    }
}
