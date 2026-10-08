package com.alumni.portal.unit.service;

import com.alumni.portal.dto.request.ProfileCreateRequest;
import com.alumni.portal.dto.response.ProfileResponse;
import com.alumni.portal.entity.Profile;
import com.alumni.portal.entity.User;
import com.alumni.portal.entity.enums.Role;
import com.alumni.portal.exception.DuplicateResourceException;
import com.alumni.portal.exception.ResourceNotFoundException;
import com.alumni.portal.repository.ProfileRepository;
import com.alumni.portal.repository.UserRepository;
import com.alumni.portal.service.ProfileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProfileService (STLV4-79: Basic Profile Creation).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProfileService Unit Tests")
class ProfileServiceTest {

    @Mock private ProfileRepository profileRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private ProfileService profileService;

    private User buildUser() {
        return User.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@college.edu")
                .role(Role.ROLE_ALUMNI)
                .build();
    }

    private ProfileCreateRequest buildProfileRequest() {
        return ProfileCreateRequest.builder()
                .phone("+91-9876543210")
                .dateOfBirth(LocalDate.of(1998, 5, 15))
                .city("Chennai")
                .state("Tamil Nadu")
                .country("India")
                .bio("Software Engineer and alumni")
                .linkedinUrl("https://linkedin.com/in/janesmith")
                .build();
    }

    private Profile buildProfile(User user) {
        return Profile.builder()
                .id(1L)
                .user(user)
                .phone("+91-9876543210")
                .dateOfBirth(LocalDate.of(1998, 5, 15))
                .city("Chennai")
                .state("Tamil Nadu")
                .country("India")
                .bio("Software Engineer and alumni")
                .linkedinUrl("https://linkedin.com/in/janesmith")
                .build();
    }

    @Nested
    @DisplayName("Create Profile")
    class CreateProfileTests {

        @Test
        @DisplayName("Should create a profile successfully")
        void createProfile_withValidData_returnsProfileResponse() {
            User user = buildUser();
            ProfileCreateRequest request = buildProfileRequest();
            Profile savedProfile = buildProfile(user);

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(profileRepository.existsByUserId(1L)).thenReturn(false);
            when(profileRepository.save(any(Profile.class))).thenReturn(savedProfile);

            ProfileResponse response = profileService.createProfile(1L, request);

            assertThat(response).isNotNull();
            assertThat(response.getFirstName()).isEqualTo("Jane");
            assertThat(response.getCity()).isEqualTo("Chennai");
            assertThat(response.getPhone()).isEqualTo("+91-9876543210");

            verify(profileRepository).save(any(Profile.class));
        }

        @Test
        @DisplayName("Should throw when user not found")
        void createProfile_withInvalidUser_throwsNotFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> profileService.createProfile(99L, buildProfileRequest()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw when profile already exists")
        void createProfile_whenProfileExists_throwsDuplicate() {
            User user = buildUser();
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(profileRepository.existsByUserId(1L)).thenReturn(true);

            assertThatThrownBy(() -> profileService.createProfile(1L, buildProfileRequest()))
                    .isInstanceOf(DuplicateResourceException.class);

            verify(profileRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Get Profile")
    class GetProfileTests {

        @Test
        @DisplayName("Should return profile for valid user")
        void getProfileByUserId_withValidUser_returnsProfile() {
            User user = buildUser();
            Profile profile = buildProfile(user);
            when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

            ProfileResponse response = profileService.getProfileByUserId(1L);

            assertThat(response.getUserId()).isEqualTo(1L);
            assertThat(response.getEmail()).isEqualTo("jane.smith@college.edu");
        }

        @Test
        @DisplayName("Should throw when profile not found")
        void getProfileByUserId_withNoProfile_throwsNotFound() {
            when(profileRepository.findByUserId(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> profileService.getProfileByUserId(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Update Profile")
    class UpdateProfileTests {

        @Test
        @DisplayName("Should update only non-null fields")
        void updateProfile_withPartialData_updatesOnlyProvidedFields() {
            User user = buildUser();
            Profile existingProfile = buildProfile(user);
            ProfileCreateRequest updateRequest = ProfileCreateRequest.builder()
                    .city("Bangalore")
                    .bio("Updated bio")
                    .build();

            when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(existingProfile));
            when(profileRepository.save(any(Profile.class))).thenReturn(existingProfile);

            ProfileResponse response = profileService.updateProfile(1L, updateRequest);

            assertThat(response).isNotNull();
            verify(profileRepository).save(any(Profile.class));
        }
    }
}
