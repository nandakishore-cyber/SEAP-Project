package com.alumni.portal.unit.service;

import com.alumni.portal.dto.request.MentorshipRequestCreateRequest;
import com.alumni.portal.entity.AlumniProfile;
import com.alumni.portal.entity.MentorshipRequest;
import com.alumni.portal.entity.User;
import com.alumni.portal.entity.enums.MentorshipRequestStatus;
import com.alumni.portal.entity.enums.Role;
import com.alumni.portal.entity.enums.VerificationStatus;
import com.alumni.portal.exception.DuplicateResourceException;
import com.alumni.portal.repository.AlumniProfileRepository;
import com.alumni.portal.repository.MentorshipRequestRepository;
import com.alumni.portal.repository.UserRepository;
import com.alumni.portal.service.AlumniService;
import com.alumni.portal.service.MentorshipService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MentorshipServiceTest {

    @Mock private MentorshipRequestRepository requestRepository;
    @Mock private AlumniProfileRepository alumniProfileRepository;
    @Mock private UserRepository userRepository;
    @Mock private AlumniService alumniService;
    @InjectMocks private MentorshipService mentorshipService;

    @Test
    void createRequest_savesPendingStudentRequest() {
        User student = user(1L, Role.ROLE_STUDENT, "Student", "User");
        AlumniProfile alumni = alumni(9L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(alumniProfileRepository.findById(9L)).thenReturn(Optional.of(alumni));
        when(requestRepository.existsByStudentIdAndAlumniProfileIdAndStatus(
                1L, 9L, MentorshipRequestStatus.PENDING)).thenReturn(false);
        when(requestRepository.save(any(MentorshipRequest.class))).thenAnswer(invocation -> {
            MentorshipRequest saved = invocation.getArgument(0);
            saved.setId(5L);
            return saved;
        });

        var response = mentorshipService.createRequest(1L,
                MentorshipRequestCreateRequest.builder()
                        .alumniProfileId(9L)
                        .message("Please guide me")
                        .goals("Backend career")
                        .build());

        assertThat(response.getId()).isEqualTo(5L);
        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getAlumniName()).isEqualTo("Alumni Mentor");
    }

    @Test
    void createRequest_rejectsDuplicatePendingRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, Role.ROLE_STUDENT, "Student", "User")));
        when(alumniProfileRepository.findById(9L)).thenReturn(Optional.of(alumni(9L)));
        when(requestRepository.existsByStudentIdAndAlumniProfileIdAndStatus(
                1L, 9L, MentorshipRequestStatus.PENDING)).thenReturn(true);

        assertThatThrownBy(() -> mentorshipService.createRequest(1L,
                MentorshipRequestCreateRequest.builder()
                        .alumniProfileId(9L).message("Again").build()))
                .isInstanceOf(DuplicateResourceException.class);
    }

    private User user(Long id, Role role, String firstName, String lastName) {
        return User.builder().id(id).role(role).firstName(firstName).lastName(lastName)
                .email(firstName.toLowerCase() + "@test.com").password("encoded").enabled(true).build();
    }

    private AlumniProfile alumni(Long id) {
        return AlumniProfile.builder().id(id).user(user(2L, Role.ROLE_ALUMNI, "Alumni", "Mentor"))
                .department("Computer Science").graduationYear(2020)
                .collegeName("College").degree("B.Tech")
                .verificationStatus(VerificationStatus.VERIFIED).currentCompany("Tech Corp")
                .currentDesignation("Engineer").build();
    }
}
