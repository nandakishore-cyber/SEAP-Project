package com.alumni.portal.service;

import com.alumni.portal.dto.request.ProfileCreateRequest;
import com.alumni.portal.dto.response.ProfileResponse;
import com.alumni.portal.entity.Profile;
import com.alumni.portal.entity.User;
import com.alumni.portal.exception.DuplicateResourceException;
import com.alumni.portal.exception.ResourceNotFoundException;
import com.alumni.portal.repository.ProfileRepository;
import com.alumni.portal.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles basic profile creation and retrieval (STLV4-79).
 */
@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    public ProfileService(ProfileRepository profileRepository,
                          UserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create a basic profile for the authenticated user (STLV4-79: Basic Profile Creation).
     */
    @Transactional
    public ProfileResponse createProfile(Long userId, ProfileCreateRequest request) {
        // Verify user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Check if profile already exists
        if (profileRepository.existsByUserId(userId)) {
            throw new DuplicateResourceException("Profile", "userId", userId);
        }

        Profile profile = Profile.builder()
                .user(user)
                .phone(request.getPhone())
                .dateOfBirth(request.getDateOfBirth())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .bio(request.getBio())
                .profilePictureUrl(request.getProfilePictureUrl())
                .linkedinUrl(request.getLinkedinUrl())
                .studentId(request.getStudentId())
                .program(request.getProgram())
                .department(request.getDepartment())
                .expectedGraduationYear(request.getExpectedGraduationYear())
                .interests(request.getInterests())
                .studentSkills(request.getStudentSkills())
                .build();

        Profile savedProfile = profileRepository.save(profile);
        return mapToResponse(savedProfile);
    }

    /**
     * Get profile of the authenticated user.
     */
    @Transactional(readOnly = true)
    public ProfileResponse getProfileByUserId(Long userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));
        return mapToResponse(profile);
    }

    /**
     * Update an existing profile.
     */
    @Transactional
    public ProfileResponse updateProfile(Long userId, ProfileCreateRequest request) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));

        if (request.getPhone() != null) profile.setPhone(request.getPhone());
        if (request.getDateOfBirth() != null) profile.setDateOfBirth(request.getDateOfBirth());
        if (request.getAddress() != null) profile.setAddress(request.getAddress());
        if (request.getCity() != null) profile.setCity(request.getCity());
        if (request.getState() != null) profile.setState(request.getState());
        if (request.getCountry() != null) profile.setCountry(request.getCountry());
        if (request.getBio() != null) profile.setBio(request.getBio());
        if (request.getProfilePictureUrl() != null) profile.setProfilePictureUrl(request.getProfilePictureUrl());
        if (request.getLinkedinUrl() != null) profile.setLinkedinUrl(request.getLinkedinUrl());
        if (request.getStudentId() != null) profile.setStudentId(request.getStudentId());
        if (request.getProgram() != null) profile.setProgram(request.getProgram());
        if (request.getDepartment() != null) profile.setDepartment(request.getDepartment());
        if (request.getExpectedGraduationYear() != null) {
            profile.setExpectedGraduationYear(request.getExpectedGraduationYear());
        }
        if (request.getInterests() != null) profile.setInterests(request.getInterests());
        if (request.getStudentSkills() != null) profile.setStudentSkills(request.getStudentSkills());

        Profile updatedProfile = profileRepository.save(profile);
        return mapToResponse(updatedProfile);
    }

    // ---- Mapper ----

    private ProfileResponse mapToResponse(Profile profile) {
        User user = profile.getUser();
        return ProfileResponse.builder()
                .id(profile.getId())
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(profile.getPhone())
                .dateOfBirth(profile.getDateOfBirth())
                .address(profile.getAddress())
                .city(profile.getCity())
                .state(profile.getState())
                .country(profile.getCountry())
                .bio(profile.getBio())
                .profilePictureUrl(profile.getProfilePictureUrl())
                .linkedinUrl(profile.getLinkedinUrl())
                .studentId(profile.getStudentId())
                .program(profile.getProgram())
                .department(profile.getDepartment())
                .expectedGraduationYear(profile.getExpectedGraduationYear())
                .interests(profile.getInterests())
                .studentSkills(profile.getStudentSkills())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
