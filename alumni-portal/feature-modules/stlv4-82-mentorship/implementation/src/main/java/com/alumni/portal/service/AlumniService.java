package com.alumni.portal.service;

import com.alumni.portal.dto.request.AlumniProfileRequest;
import com.alumni.portal.dto.request.VerificationRequest;
import com.alumni.portal.dto.response.AlumniProfileResponse;
import com.alumni.portal.entity.AlumniProfile;
import com.alumni.portal.entity.User;
import com.alumni.portal.entity.enums.VerificationStatus;
import com.alumni.portal.exception.DuplicateResourceException;
import com.alumni.portal.exception.ResourceNotFoundException;
import com.alumni.portal.repository.AlumniProfileRepository;
import com.alumni.portal.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Handles alumni profile creation (STLV4-61), verification (STLV4-24),
 * and viewing (STLV4-17).
 */
@Service
public class AlumniService {

    private final AlumniProfileRepository alumniProfileRepository;
    private final UserRepository userRepository;

    public AlumniService(AlumniProfileRepository alumniProfileRepository,
                         UserRepository userRepository) {
        this.alumniProfileRepository = alumniProfileRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create an alumni profile (STLV4-61: Create Alumni Profile).
     */
    @Transactional
    public AlumniProfileResponse createAlumniProfile(Long userId, AlumniProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (alumniProfileRepository.existsByUserId(userId)) {
            throw new DuplicateResourceException("AlumniProfile", "userId", userId);
        }

        AlumniProfile profile = AlumniProfile.builder()
                .user(user)
                .collegeName(request.getCollegeName())
                .department(request.getDepartment())
                .degree(request.getDegree())
                .graduationYear(request.getGraduationYear())
                .rollNumber(request.getRollNumber())
                .currentCompany(request.getCurrentCompany())
                .currentDesignation(request.getCurrentDesignation())
                .industry(request.getIndustry())
                .yearsOfExperience(request.getYearsOfExperience() != null ? request.getYearsOfExperience() : 0)
                .skills(request.getSkills())
                .verificationStatus(VerificationStatus.PENDING)
                .build();

        AlumniProfile saved = alumniProfileRepository.save(profile);
        return mapToResponse(saved);
    }

    /**
     * Get alumni profile by ID (STLV4-17: View Alumni Profile).
     */
    @Transactional(readOnly = true)
    public AlumniProfileResponse getAlumniProfileById(Long id) {
        AlumniProfile profile = alumniProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AlumniProfile", "id", id));
        return mapToResponse(profile);
    }

    /**
     * Get alumni profile for the authenticated user.
     */
    @Transactional(readOnly = true)
    public AlumniProfileResponse getAlumniProfileByUserId(Long userId) {
        AlumniProfile profile = alumniProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("AlumniProfile", "userId", userId));
        return mapToResponse(profile);
    }

    /**
     * Search/list verified alumni profiles (STLV4-17: View Alumni Profile — subtasks).
     */
    @Transactional(readOnly = true)
    public Page<AlumniProfileResponse> searchAlumni(String department,
                                                     Integer graduationYear,
                                                     String company,
                                                     Pageable pageable) {
        Page<AlumniProfile> profiles = alumniProfileRepository.searchAlumni(
                department, graduationYear, company, pageable);
        return profiles.map(this::mapToResponse);
    }

    /**
     * List all alumni with a specific verification status (admin use).
     */
    @Transactional(readOnly = true)
    public Page<AlumniProfileResponse> getAlumniByStatus(VerificationStatus status, Pageable pageable) {
        return alumniProfileRepository.findByVerificationStatus(status, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Verify or reject an alumni profile (STLV4-24: Alumni Profile Verification).
     * Admin only.
     */
    @Transactional
    public AlumniProfileResponse verifyAlumniProfile(Long alumniId, VerificationRequest request) {
        AlumniProfile profile = alumniProfileRepository.findById(alumniId)
                .orElseThrow(() -> new ResourceNotFoundException("AlumniProfile", "id", alumniId));

        if (request.getApproved()) {
            profile.setVerificationStatus(VerificationStatus.VERIFIED);
        } else {
            profile.setVerificationStatus(VerificationStatus.REJECTED);
        }
        profile.setVerificationRemarks(request.getRemarks());
        profile.setVerifiedAt(LocalDateTime.now());

        AlumniProfile updated = alumniProfileRepository.save(profile);
        return mapToResponse(updated);
    }

    // ---- Mapper ----

    private AlumniProfileResponse mapToResponse(AlumniProfile profile) {
        User user = profile.getUser();
        return AlumniProfileResponse.builder()
                .id(profile.getId())
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .collegeName(profile.getCollegeName())
                .department(profile.getDepartment())
                .degree(profile.getDegree())
                .graduationYear(profile.getGraduationYear())
                .rollNumber(profile.getRollNumber())
                .currentCompany(profile.getCurrentCompany())
                .currentDesignation(profile.getCurrentDesignation())
                .industry(profile.getIndustry())
                .yearsOfExperience(profile.getYearsOfExperience())
                .skills(profile.getSkills())
                .verificationStatus(profile.getVerificationStatus().name())
                .verificationRemarks(profile.getVerificationRemarks())
                .verifiedAt(profile.getVerifiedAt())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
