package com.alumni.portal.service;

import com.alumni.portal.dto.request.MentorshipRequestCreateRequest;
import com.alumni.portal.dto.request.MentorshipRequestDecisionRequest;
import com.alumni.portal.dto.response.AlumniProfileResponse;
import com.alumni.portal.dto.response.MentorshipRequestResponse;
import com.alumni.portal.entity.AlumniProfile;
import com.alumni.portal.entity.MentorshipRequest;
import com.alumni.portal.entity.User;
import com.alumni.portal.entity.enums.MentorshipRequestStatus;
import com.alumni.portal.entity.enums.Role;
import com.alumni.portal.entity.enums.VerificationStatus;
import com.alumni.portal.exception.DuplicateResourceException;
import com.alumni.portal.exception.ResourceNotFoundException;
import com.alumni.portal.repository.AlumniProfileRepository;
import com.alumni.portal.repository.MentorshipRequestRepository;
import com.alumni.portal.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MentorshipService {

    private final MentorshipRequestRepository requestRepository;
    private final AlumniProfileRepository alumniProfileRepository;
    private final UserRepository userRepository;
    private final AlumniService alumniService;
    private final NotificationService notificationService;

    @Autowired
    public MentorshipService(MentorshipRequestRepository requestRepository,
                             AlumniProfileRepository alumniProfileRepository,
                             UserRepository userRepository,
                             AlumniService alumniService,
                             NotificationService notificationService) {
        this.requestRepository = requestRepository;
        this.alumniProfileRepository = alumniProfileRepository;
        this.userRepository = userRepository;
        this.alumniService = alumniService;
        this.notificationService = notificationService;
    }

    public MentorshipService(MentorshipRequestRepository requestRepository,
                             AlumniProfileRepository alumniProfileRepository,
                             UserRepository userRepository,
                             AlumniService alumniService) {
        this(requestRepository, alumniProfileRepository, userRepository, alumniService, null);
    }

    @Transactional(readOnly = true)
    public List<AlumniProfileResponse> getAvailableMentors() {
        return alumniService.searchAlumni(null, null, null, PageRequest.of(0, 100))
                .getContent();
    }

    @Transactional
    public MentorshipRequestResponse createRequest(Long studentId,
                                                    MentorshipRequestCreateRequest request) {
        User student = findUser(studentId);
        requireRole(student, Role.ROLE_STUDENT);
        AlumniProfile alumni = alumniProfileRepository.findById(request.getAlumniProfileId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "AlumniProfile", "id", request.getAlumniProfileId()));

        if (alumni.getVerificationStatus() != VerificationStatus.VERIFIED) {
            throw new ResourceNotFoundException("AlumniProfile", "id", request.getAlumniProfileId());
        }
        if (requestRepository.existsByStudentIdAndAlumniProfileIdAndStatus(
                studentId, alumni.getId(), MentorshipRequestStatus.PENDING)) {
            throw new DuplicateResourceException("MentorshipRequest", "studentAndAlumni", studentId);
        }

        MentorshipRequest saved = requestRepository.save(MentorshipRequest.builder()
                .student(student)
                .alumniProfile(alumni)
                .message(request.getMessage())
                .goals(request.getGoals())
                .status(MentorshipRequestStatus.PENDING)
                .build());
        MentorshipRequestResponse response = map(saved);
        if (notificationService != null) {
            notificationService.notify(alumni.getUser().getId(), "MENTORSHIP_REQUEST",
                    "New mentorship request",
                    student.getFirstName() + " " + student.getLastName() + " sent you a mentorship request.",
                    saved.getId());
        }
        return response;
    }

    @Transactional(readOnly = true)
    public List<MentorshipRequestResponse> getStudentRequests(Long studentId) {
        requireRole(findUser(studentId), Role.ROLE_STUDENT);
        return requestRepository.findByStudentIdOrderByCreatedAtDesc(studentId)
                .stream().map(this::map).toList();
    }

    @Transactional(readOnly = true)
    public List<MentorshipRequestResponse> getAlumniRequests(Long alumniUserId) {
        requireRole(findUser(alumniUserId), Role.ROLE_ALUMNI);
        return requestRepository.findByAlumniProfileUserIdOrderByCreatedAtDesc(alumniUserId)
                .stream().map(this::map).toList();
    }

    @Transactional
    public MentorshipRequestResponse decideRequest(Long alumniUserId, Long requestId,
                                                     MentorshipRequestDecisionRequest decision) {
        User alumniUser = findUser(alumniUserId);
        requireRole(alumniUser, Role.ROLE_ALUMNI);
        MentorshipRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("MentorshipRequest", "id", requestId));
        if (!request.getAlumniProfile().getUser().getId().equals(alumniUserId)) {
            throw new ResourceNotFoundException("MentorshipRequest", "id", requestId);
        }
        if (decision.getStatus() == MentorshipRequestStatus.PENDING) {
            throw new IllegalArgumentException("A mentorship request must be accepted or rejected");
        }
        request.setStatus(decision.getStatus());
        request.setResponseMessage(decision.getResponseMessage());
        MentorshipRequestResponse response = map(requestRepository.save(request));
        if (notificationService != null) {
            notificationService.notify(request.getStudent().getId(), "MENTORSHIP_DECISION",
                    "Mentorship request " + decision.getStatus().name().toLowerCase(),
                    alumniUserName(request) + " " + decision.getStatus().name().toLowerCase()
                            + " your mentorship request.",
                    request.getId());
        }
        return response;
    }

    private String alumniUserName(MentorshipRequest request) {
        User user = request.getAlumniProfile().getUser();
        return user.getFirstName() + " " + user.getLastName();
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    private void requireRole(User user, Role role) {
        if (user.getRole() != role) {
            throw new IllegalArgumentException("This operation is not available for the current role");
        }
    }

    private MentorshipRequestResponse map(MentorshipRequest request) {
        User student = request.getStudent();
        AlumniProfile alumni = request.getAlumniProfile();
        User alumniUser = alumni.getUser();
        return MentorshipRequestResponse.builder()
                .id(request.getId())
                .studentId(student.getId())
                .studentName(student.getFirstName() + " " + student.getLastName())
                .alumniProfileId(alumni.getId())
                .alumniName(alumniUser.getFirstName() + " " + alumniUser.getLastName())
                .alumniCompany(alumni.getCurrentCompany())
                .alumniDesignation(alumni.getCurrentDesignation())
                .message(request.getMessage())
                .goals(request.getGoals())
                .status(request.getStatus().name())
                .responseMessage(request.getResponseMessage())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }
}
