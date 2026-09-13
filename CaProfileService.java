package com.fintax.service;

import com.fintax.dto.CaProfileRequest;
import com.fintax.dto.CaProfileResponse;
import com.fintax.entity.CaProfile;
import com.fintax.entity.CaSpecialization;
import com.fintax.exception.ApiException;
import com.fintax.repository.CaProfileRepository;
import com.fintax.repository.CaSpecializationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CaProfileService {

    private final CaProfileRepository caProfileRepository;
    private final CaSpecializationRepository caSpecializationRepository;

    public List<CaProfileResponse> search(String city, String state, String pincode, String specialization) {
        List<CaProfile> results = caProfileRepository.search(
                blankToNull(city), blankToNull(state), blankToNull(pincode), blankToNull(specialization)
        );
        return results.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public CaProfileResponse getById(Long id) {
        CaProfile profile = caProfileRepository.findById(id)
                .orElseThrow(() -> new ApiException("CA profile not found", HttpStatus.NOT_FOUND));
        return toResponse(profile);
    }

    public List<CaProfileResponse> getAll() {
        return caProfileRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public CaProfileResponse create(CaProfileRequest request) {
        CaProfile profile = new CaProfile();
        applyRequest(profile, request);
        return toResponse(caProfileRepository.save(profile));
    }

    public CaProfileResponse update(Long id, CaProfileRequest request) {
        CaProfile profile = caProfileRepository.findById(id)
                .orElseThrow(() -> new ApiException("CA profile not found", HttpStatus.NOT_FOUND));
        applyRequest(profile, request);
        return toResponse(caProfileRepository.save(profile));
    }

    public CaProfileResponse setVerified(Long id, boolean verified) {
        CaProfile profile = caProfileRepository.findById(id)
                .orElseThrow(() -> new ApiException("CA profile not found", HttpStatus.NOT_FOUND));
        profile.setIsVerified(verified);
        return toResponse(caProfileRepository.save(profile));
    }

    public void delete(Long id) {
        if (!caProfileRepository.existsById(id)) {
            throw new ApiException("CA profile not found", HttpStatus.NOT_FOUND);
        }
        caProfileRepository.deleteById(id);
    }

    private void applyRequest(CaProfile profile, CaProfileRequest request) {
        profile.setFullName(request.getFullName());
        profile.setQualification(request.getQualification());
        profile.setCity(request.getCity());
        profile.setState(request.getState());
        profile.setPincode(request.getPincode());
        profile.setExperienceYears(request.getExperienceYears());
        profile.setLanguages(request.getLanguages());
        profile.setBio(request.getBio());
        profile.setContactEmail(request.getContactEmail());
        profile.setContactPhone(request.getContactPhone());

        if (request.getSpecializationNames() != null) {
            Set<CaSpecialization> specializations = new HashSet<>();
            for (String name : request.getSpecializationNames()) {
                CaSpecialization spec = caSpecializationRepository.findByNameIgnoreCase(name)
                        .orElseThrow(() -> new ApiException("Unknown specialization: " + name, HttpStatus.BAD_REQUEST));
                specializations.add(spec);
            }
            profile.setSpecializations(specializations);
        }
    }

    private CaProfileResponse toResponse(CaProfile p) {
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

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
