package com.fintax.service;

import com.fintax.dto.ContactRequestDto;
import com.fintax.entity.CaProfile;
import com.fintax.entity.ContactRequest;
import com.fintax.entity.User;
import com.fintax.exception.ApiException;
import com.fintax.repository.CaProfileRepository;
import com.fintax.repository.ContactRequestRepository;
import com.fintax.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContactRequestService {

    private final ContactRequestRepository contactRequestRepository;
    private final CaProfileRepository caProfileRepository;
    private final CurrentUserService currentUserService;

    public void createRequest(Long caProfileId, ContactRequestDto dto) {
        CaProfile caProfile = caProfileRepository.findById(caProfileId)
                .orElseThrow(() -> new ApiException("CA profile not found", HttpStatus.NOT_FOUND));

        ContactRequest request = new ContactRequest();
        request.setCaProfile(caProfile);
        request.setName(dto.getName());
        request.setEmail(dto.getEmail());
        request.setPhone(dto.getPhone());
        request.setMessage(dto.getMessage());

        // Attach logged-in user if authenticated; guests can still submit requests
        try {
            User currentUser = currentUserService.getCurrentUser();
            request.setUser(currentUser);
        } catch (Exception ignored) {
            // no authenticated user — proceed as guest
        }

        contactRequestRepository.save(request);
    }
}
