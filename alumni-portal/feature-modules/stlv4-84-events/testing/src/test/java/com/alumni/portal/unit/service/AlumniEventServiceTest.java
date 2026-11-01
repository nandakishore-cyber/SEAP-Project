package com.alumni.portal.unit.service;

import com.alumni.portal.dto.request.AlumniEventRequest;
import com.alumni.portal.dto.request.EventRegistrationRequest;
import com.alumni.portal.entity.AlumniEvent;
import com.alumni.portal.entity.EventRegistration;
import com.alumni.portal.entity.User;
import com.alumni.portal.entity.enums.Role;
import com.alumni.portal.repository.AlumniEventRepository;
import com.alumni.portal.repository.EventRegistrationRepository;
import com.alumni.portal.repository.UserRepository;
import com.alumni.portal.service.AlumniEventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlumniEventServiceTest {

    @Mock private AlumniEventRepository eventRepository;
    @Mock private EventRegistrationRepository registrationRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private AlumniEventService eventService;

    @Test
    void createEvent_savesEventForAlumni() {
        User alumni = user(1L, Role.ROLE_ALUMNI);
        when(userRepository.findById(1L)).thenReturn(Optional.of(alumni));
        when(eventRepository.save(any(AlumniEvent.class))).thenAnswer(invocation -> {
            AlumniEvent event = invocation.getArgument(0);
            event.setId(4L);
            return event;
        });
        AlumniEventRequest request = request();

        var response = eventService.createEvent(1L, request);

        assertThat(response.getId()).isEqualTo(4L);
        assertThat(response.getTitle()).isEqualTo("Career Workshop");
    }

    @Test
    void register_savesStudentRegistration() {
        User student = user(2L, Role.ROLE_STUDENT);
        AlumniEvent event = AlumniEvent.builder()
                .id(4L).title("Career Workshop").eventDate(LocalDateTime.now().plusDays(2))
                .registrationDeadline(LocalDateTime.now().plusDays(1)).capacity(10)
                .location("Hall").mode("HYBRID").organizer(user(1L, Role.ROLE_ALUMNI)).build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));
        when(eventRepository.findById(4L)).thenReturn(Optional.of(event));
        when(registrationRepository.existsByEventIdAndStudentId(4L, 2L)).thenReturn(false);
        when(registrationRepository.countByEventId(4L)).thenReturn(0L);
        when(registrationRepository.save(any(EventRegistration.class))).thenAnswer(invocation -> {
            EventRegistration registration = invocation.getArgument(0);
            registration.setId(7L);
            return registration;
        });

        var response = eventService.register(2L, 4L, EventRegistrationRequest.builder()
                .attendanceMode("ONLINE").currentSemester("Semester 5")
                .expectations("Career guidance").build());

        assertThat(response.getId()).isEqualTo(7L);
        assertThat(response.getEventTitle()).isEqualTo("Career Workshop");
    }

    private AlumniEventRequest request() {
        return AlumniEventRequest.builder().title("Career Workshop")
                .description("Learn from alumni")
                .eventDate(LocalDateTime.now().plusDays(4))
                .registrationDeadline(LocalDateTime.now().plusDays(2))
                .location("Hall").mode("HYBRID").capacity(20).build();
    }

    private User user(Long id, Role role) {
        return User.builder().id(id).role(role).firstName("Test").lastName("User")
                .email(id + "@test.com").password("encoded").enabled(true).build();
    }
}
