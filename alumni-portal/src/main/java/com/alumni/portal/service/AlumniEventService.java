package com.alumni.portal.service;

import com.alumni.portal.dto.request.AlumniEventRequest;
import com.alumni.portal.dto.request.EventRegistrationRequest;
import com.alumni.portal.dto.response.AlumniEventResponse;
import com.alumni.portal.dto.response.EventRegistrationResponse;
import com.alumni.portal.entity.AlumniEvent;
import com.alumni.portal.entity.EventRegistration;
import com.alumni.portal.entity.User;
import com.alumni.portal.entity.enums.Role;
import com.alumni.portal.exception.DuplicateResourceException;
import com.alumni.portal.exception.ResourceNotFoundException;
import com.alumni.portal.repository.AlumniEventRepository;
import com.alumni.portal.repository.EventRegistrationRepository;
import com.alumni.portal.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlumniEventService {

    private final AlumniEventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final UserRepository userRepository;

    public AlumniEventService(AlumniEventRepository eventRepository,
                              EventRegistrationRepository registrationRepository,
                              UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<AlumniEventResponse> getUpcomingEvents(Long studentId) {
        return eventRepository.findByEventDateAfterOrderByEventDateAsc(LocalDateTime.now())
                .stream().map(event -> mapEvent(event, studentId)).toList();
    }

    @Transactional
    public AlumniEventResponse createEvent(Long organizerId, AlumniEventRequest request) {
        User organizer = findUser(organizerId);
        if (organizer.getRole() != Role.ROLE_ADMIN && organizer.getRole() != Role.ROLE_ALUMNI) {
            throw new IllegalArgumentException("Only alumni or administrators can create events");
        }
        if (!request.getRegistrationDeadline().isBefore(request.getEventDate())) {
            throw new IllegalArgumentException("Registration deadline must be before the event date");
        }
        AlumniEvent saved = eventRepository.save(AlumniEvent.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .eventDate(request.getEventDate())
                .location(request.getLocation())
                .mode(request.getMode())
                .capacity(request.getCapacity())
                .registrationDeadline(request.getRegistrationDeadline())
                .organizer(organizer)
                .build());
        return mapEvent(saved, null);
    }

    @Transactional
    public EventRegistrationResponse register(Long studentId, Long eventId,
                                               EventRegistrationRequest request) {
        User student = findUser(studentId);
        if (student.getRole() != Role.ROLE_STUDENT) {
            throw new IllegalArgumentException("Only students can register for events");
        }
        AlumniEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("AlumniEvent", "id", eventId));
        if (registrationRepository.existsByEventIdAndStudentId(eventId, studentId)) {
            throw new DuplicateResourceException("EventRegistration", "eventAndStudent", eventId);
        }
        if (!LocalDateTime.now().isBefore(event.getRegistrationDeadline())) {
            throw new IllegalArgumentException("Registration for this event has closed");
        }
        if (registrationRepository.countByEventId(eventId) >= event.getCapacity()) {
            throw new IllegalArgumentException("This event is full");
        }
        EventRegistration saved = registrationRepository.save(EventRegistration.builder()
                .event(event)
                .student(student)
                .attendanceMode(request.getAttendanceMode())
                .currentSemester(request.getCurrentSemester())
                .expectations(request.getExpectations())
                .dietaryPreference(request.getDietaryPreference())
                .build());
        return mapRegistration(saved);
    }

    @Transactional(readOnly = true)
    public List<EventRegistrationResponse> getRegistrations(Long studentId) {
        if (findUser(studentId).getRole() != Role.ROLE_STUDENT) {
            throw new IllegalArgumentException("Only students can view event registrations");
        }
        return registrationRepository.findByStudentIdOrderByRegisteredAtDesc(studentId)
                .stream().map(this::mapRegistration).toList();
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    private AlumniEventResponse mapEvent(AlumniEvent event, Long studentId) {
        User organizer = event.getOrganizer();
        return AlumniEventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .location(event.getLocation())
                .mode(event.getMode())
                .capacity(event.getCapacity())
                .registeredCount(registrationRepository.countByEventId(event.getId()))
                .registrationDeadline(event.getRegistrationDeadline())
                .organizerName(organizer.getFirstName() + " " + organizer.getLastName())
                .registered(studentId != null &&
                        registrationRepository.existsByEventIdAndStudentId(event.getId(), studentId))
                .build();
    }

    private EventRegistrationResponse mapRegistration(EventRegistration registration) {
        return EventRegistrationResponse.builder()
                .id(registration.getId())
                .eventId(registration.getEvent().getId())
                .eventTitle(registration.getEvent().getTitle())
                .attendanceMode(registration.getAttendanceMode())
                .currentSemester(registration.getCurrentSemester())
                .expectations(registration.getExpectations())
                .dietaryPreference(registration.getDietaryPreference())
                .registeredAt(registration.getRegisteredAt())
                .build();
    }
}
