package com.fintax.service;

import com.fintax.dto.NotificationResponse;
import com.fintax.entity.Notification;
import com.fintax.entity.User;
import com.fintax.exception.ApiException;
import com.fintax.repository.NotificationRepository;
import com.fintax.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final CurrentUserService currentUserService;

    public List<NotificationResponse> getAll() {
        User user = currentUserService.getCurrentUser();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public long getUnreadCount() {
        User user = currentUserService.getCurrentUser();
        return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
    }

    public void markAsRead(Long notificationId) {
        User user = currentUserService.getCurrentUser();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ApiException("Notification not found", HttpStatus.NOT_FOUND));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new ApiException("Not authorized to modify this notification", HttpStatus.FORBIDDEN);
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(n.getId(), n.getTitle(), n.getMessage(), n.getIsRead(), n.getCreatedAt());
    }
}
