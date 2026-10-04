package com.alumni.portal.unit.service;

import com.alumni.portal.dto.request.AlumniProfileRequest;
import com.alumni.portal.dto.request.VerificationRequest;
import com.alumni.portal.dto.response.AlumniProfileResponse;
import com.alumni.portal.entity.AlumniProfile;
import com.alumni.portal.entity.User;
import com.alumni.portal.entity.enums.Role;
import com.alumni.portal.entity.enums.VerificationStatus;
import com.alumni.portal.exception.DuplicateResourceException;
import com.alumni.portal.exception.ResourceNotFoundException;
import com.alumni.portal.repository.AlumniProfileRepository;
import com.alumni.portal.repository.UserRepository;
import com.alumni.portal.service.AlumniService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AlumniService.
 * Covers: STLV4-61 (Create), STLV4-24 (Verify), STLV4-17 (View).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AlumniService Unit Tests")
class AlumniServiceTest {

    @Mock private AlumniProfileRepository alumniProfileRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private AlumniService alumniService;

    private User buildUser() {
        return User.builder()
                .id(1L)
                .firstName("Ravi")
                .lastName("Kumar")
                .email("ravi.kumar@college.edu")
                .role(Role.ROLE_ALUMNI)
                .build();
    }

    private AlumniProfileRequest buildAlumniRequest() {
        return AlumniProfileRequest.builder()
                .collegeName("ABC Engineering College")
                .department("Computer Science")
                .degree("B.Tech")
                .graduationYear(2020)
                .rollNumber("CS2020001")
                .currentCompany("Google")
                .currentDesignation("Software Engineer")
                .industry("Technology")
                .yearsOfExperience(4)
                .skills("Java, Spring Boot, Microservices")
                .build();
    }

    private AlumniProfile buildAlumniProfile(User user) {
        return AlumniProfile.builder()
                .id(1L)
                .user(user)
                .collegeName("ABC Engineering College")
                .department("Computer Science")
                .degree("B.Tech")
                .graduationYear(2020)
                .rollNumber("CS2020001")
                .currentCompany("Google")
                .currentDesignation("Software Engineer")
                .industry("Technology")
                .yearsOfExperience(4)
                .skills("Java, Spring Boot, Microservices")
                .verificationStatus(VerificationStatus.PENDING)
                .build();
    }

    @Nested
    @DisplayName("STLV4-61: Create Alumni Profile")
    class CreateAlumniProfileTests {

        @Test
        @DisplayName("Should create alumni profile successfully")
        void createAlumniProfile_withValidData_returnsResponse() {
            User user = buildUser();
            AlumniProfileRequest request = buildAlumniRequest();
            AlumniProfile saved = buildAlumniProfile(user);

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(alumniProfileRepository.existsByUserId(1L)).thenReturn(false);
            when(alumniProfileRepository.save(any(AlumniProfile.class))).thenReturn(saved);

            AlumniProfileResponse response = alumniService.createAlumniProfile(1L, request);

            assertThat(response).isNotNull();
            assertThat(response.getCollegeName()).isEqualTo("ABC Engineering College");
            assertThat(response.getDepartment()).isEqualTo("Computer Science");
            assertThat(response.getVerificationStatus()).isEqualTo("PENDING");
            assertThat(response.getCurrentCompany()).isEqualTo("Google");

            verify(alumniProfileRepository).save(any(AlumniProfile.class));
        }

        @Test
        @DisplayName("Should throw when user not found")
        void createAlumniProfile_userNotFound_throwsException() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> alumniService.createAlumniProfile(99L, buildAlumniRequest()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw when alumni profile already exists")
        void createAlumniProfile_alreadyExists_throwsDuplicate() {
            User user = buildUser();
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(alumniProfileRepository.existsByUserId(1L)).thenReturn(true);

            assertThatThrownBy(() -> alumniService.createAlumniProfile(1L, buildAlumniRequest()))
                    .isInstanceOf(DuplicateResourceException.class);
        }

        @Test
        @DisplayName("Should set verification status to PENDING on creation")
        void createAlumniProfile_setsStatusToPending() {
            User user = buildUser();
            AlumniProfile saved = buildAlumniProfile(user);

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(alumniProfileRepository.existsByUserId(1L)).thenReturn(false);
            when(alumniProfileRepository.save(any(AlumniProfile.class))).thenReturn(saved);

            AlumniProfileResponse response = alumniService.createAlumniProfile(1L, buildAlumniRequest());

            assertThat(response.getVerificationStatus()).isEqualTo("PENDING");
        }
    }

    @Nested
    @DisplayName("STLV4-17: View Alumni Profile")
    class ViewAlumniProfileTests {

        @Test
        @DisplayName("Should return alumni profile by ID")
        void getAlumniProfileById_returnsProfile() {
            User user = buildUser();
            AlumniProfile profile = buildAlumniProfile(user);
            when(alumniProfileRepository.findById(1L)).thenReturn(Optional.of(profile));

            AlumniProfileResponse response = alumniService.getAlumniProfileById(1L);

            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getFirstName()).isEqualTo("Ravi");
        }

        @Test
        @DisplayName("Should throw when profile not found by ID")
        void getAlumniProfileById_notFound_throwsException() {
            when(alumniProfileRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> alumniService.getAlumniProfileById(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should return alumni profile by userId")
        void getAlumniProfileByUserId_returnsProfile() {
            User user = buildUser();
            AlumniProfile profile = buildAlumniProfile(user);
            when(alumniProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

            AlumniProfileResponse response = alumniService.getAlumniProfileByUserId(1L);

            assertThat(response.getUserId()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("STLV4-24: Alumni Profile Verification")
    class VerificationTests {

        @Test
        @DisplayName("Should approve alumni profile")
        void verifyAlumniProfile_approve_setsVerified() {
            User user = buildUser();
            AlumniProfile profile = buildAlumniProfile(user);
            VerificationRequest request = VerificationRequest.builder()
                    .approved(true)
                    .remarks("Documents verified successfully")
                    .build();

            when(alumniProfileRepository.findById(1L)).thenReturn(Optional.of(profile));
            when(alumniProfileRepository.save(any(AlumniProfile.class))).thenAnswer(inv -> {
                AlumniProfile p = inv.getArgument(0);
                return p;
            });

            AlumniProfileResponse response = alumniService.verifyAlumniProfile(1L, request);

            assertThat(response.getVerificationStatus()).isEqualTo("VERIFIED");
            assertThat(response.getVerificationRemarks()).isEqualTo("Documents verified successfully");
        }

        @Test
        @DisplayName("Should reject alumni profile")
        void verifyAlumniProfile_reject_setsRejected() {
            User user = buildUser();
            AlumniProfile profile = buildAlumniProfile(user);
            VerificationRequest request = VerificationRequest.builder()
                    .approved(false)
                    .remarks("Insufficient documentation")
                    .build();

            when(alumniProfileRepository.findById(1L)).thenReturn(Optional.of(profile));
            when(alumniProfileRepository.save(any(AlumniProfile.class))).thenAnswer(inv -> inv.getArgument(0));

            AlumniProfileResponse response = alumniService.verifyAlumniProfile(1L, request);

            assertThat(response.getVerificationStatus()).isEqualTo("REJECTED");
            assertThat(response.getVerificationRemarks()).isEqualTo("Insufficient documentation");
        }

        @Test
        @DisplayName("Should throw when verifying non-existent profile")
        void verifyAlumniProfile_notFound_throwsException() {
            VerificationRequest request = VerificationRequest.builder()
                    .approved(true)
                    .remarks("Verified")
                    .build();

            when(alumniProfileRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> alumniService.verifyAlumniProfile(99L, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
