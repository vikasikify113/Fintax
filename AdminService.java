package com.fintax.service;

import com.fintax.dto.*;
import com.fintax.entity.ContactRequest;
import com.fintax.entity.Notification;
import com.fintax.entity.User;
import com.fintax.exception.ApiException;
import com.fintax.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final ContactRequestRepository contactRequestRepository;
    private final NotificationRepository notificationRepository;
    private final SearchHistoryRepository searchHistoryRepository;

    // ---------- Users ----------
    public List<UserSummaryResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(u -> new UserSummaryResponse(u.getId(), u.getFullName(), u.getEmail(),
                        u.getRole().getName(), u.getIsActive()))
                .collect(Collectors.toList());
    }

    public void setUserActive(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        user.setIsActive(active);
        userRepository.save(user);
    }

    // ---------- Reviews ----------
    public void deleteReview(Long reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new ApiException("Review not found", HttpStatus.NOT_FOUND);
        }
        reviewRepository.deleteById(reviewId);
    }

    // ---------- Contact Requests ----------
    public List<ContactRequestResponse> getAllContactRequests() {
        return contactRequestRepository.findAll().stream()
                .map(this::toContactResponse)
                .collect(Collectors.toList());
    }

    public ContactRequestResponse updateContactRequestStatus(Long id, String status) {
        ContactRequest request = contactRequestRepository.findById(id)
                .orElseThrow(() -> new ApiException("Contact request not found", HttpStatus.NOT_FOUND));
        request.setStatus(ContactRequest.Status.valueOf(status.toUpperCase()));
        return toContactResponse(contactRequestRepository.save(request));
    }

    private ContactRequestResponse toContactResponse(ContactRequest r) {
        return new ContactRequestResponse(
                r.getId(), r.getCaProfile().getId(), r.getCaProfile().getFullName(),
                r.getName(), r.getEmail(), r.getPhone(), r.getMessage(),
                r.getStatus().name(), r.getCreatedAt()
        );
    }

    // ---------- Notifications ----------
    public void sendNotification(NotificationCreateRequest request) {
        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
            notificationRepository.save(buildNotification(user, request));
        } else {
            List<User> allUsers = userRepository.findAll();
            for (User user : allUsers) {
                notificationRepository.save(buildNotification(user, request));
            }
        }
    }

    private Notification buildNotification(User user, NotificationCreateRequest request) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        return notification;
    }

    // ---------- Search Statistics ----------
    public List<Map<String, Object>> getTopSearchQueries() {
        return searchHistoryRepository.findTopQueries().stream()
                .map(q -> Map.<String, Object>of("query", q.getQuery(), "count", q.getTotal()))
                .collect(Collectors.toList());
    }
}
