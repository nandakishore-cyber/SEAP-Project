package com.alumni.portal.controller;

import com.alumni.portal.dto.request.MentorshipRequestCreateRequest;
import com.alumni.portal.dto.request.MentorshipRequestDecisionRequest;
import com.alumni.portal.dto.response.AlumniProfileResponse;
import com.alumni.portal.dto.response.ApiResponse;
import com.alumni.portal.dto.response.MentorshipRequestResponse;
import com.alumni.portal.entity.User;
import com.alumni.portal.exception.ResourceNotFoundException;
import com.alumni.portal.repository.UserRepository;
import com.alumni.portal.service.MentorshipService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mentorship")
public class MentorshipController {

    private final MentorshipService mentorshipService;
    private final UserRepository userRepository;

    public MentorshipController(MentorshipService mentorshipService, UserRepository userRepository) {
        this.mentorshipService = mentorshipService;
        this.userRepository = userRepository;
    }

    @GetMapping("/mentors")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<AlumniProfileResponse>>> getAvailableMentors() {
        return ResponseEntity.ok(ApiResponse.success(
                "Available mentors retrieved", mentorshipService.getAvailableMentors()));
    }

    @PostMapping("/requests")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<MentorshipRequestResponse>> createRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MentorshipRequestCreateRequest request) {
        MentorshipRequestResponse response = mentorshipService.createRequest(
                getUserId(userDetails), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Mentorship request sent", response));
    }

    @GetMapping("/requests/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<MentorshipRequestResponse>>> getMyRequests(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Mentorship requests retrieved",
                mentorshipService.getStudentRequests(getUserId(userDetails))));
    }

    @GetMapping("/requests/received")
    @PreAuthorize("hasRole('ALUMNI')")
    public ResponseEntity<ApiResponse<List<MentorshipRequestResponse>>> getReceivedRequests(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Received mentorship requests retrieved",
                mentorshipService.getAlumniRequests(getUserId(userDetails))));
    }

    @PutMapping("/requests/{id}")
    @PreAuthorize("hasRole('ALUMNI')")
    public ResponseEntity<ApiResponse<MentorshipRequestResponse>> decideRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody MentorshipRequestDecisionRequest decision) {
        return ResponseEntity.ok(ApiResponse.success("Mentorship request updated",
                mentorshipService.decideRequest(getUserId(userDetails), id, decision)));
    }

    private Long getUserId(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userDetails.getUsername()));
        return user.getId();
    }
}
