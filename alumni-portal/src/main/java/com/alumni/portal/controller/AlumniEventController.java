package com.alumni.portal.controller;

import com.alumni.portal.dto.request.AlumniEventRequest;
import com.alumni.portal.dto.request.EventRegistrationRequest;
import com.alumni.portal.dto.response.AlumniEventResponse;
import com.alumni.portal.dto.response.ApiResponse;
import com.alumni.portal.dto.response.EventRegistrationResponse;
import com.alumni.portal.entity.User;
import com.alumni.portal.exception.ResourceNotFoundException;
import com.alumni.portal.repository.UserRepository;
import com.alumni.portal.service.AlumniEventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class AlumniEventController {

    private final AlumniEventService eventService;
    private final UserRepository userRepository;

    public AlumniEventController(AlumniEventService eventService, UserRepository userRepository) {
        this.eventService = eventService;
        this.userRepository = userRepository;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<AlumniEventResponse>>> getUpcomingEvents(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Upcoming alumni events retrieved",
                eventService.getUpcomingEvents(getUserId(userDetails))));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ALUMNI')")
    public ResponseEntity<ApiResponse<AlumniEventResponse>> createEvent(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AlumniEventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Alumni event created", eventService.createEvent(getUserId(userDetails), request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ALUMNI')")
    public ResponseEntity<ApiResponse<AlumniEventResponse>> updateEvent(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody AlumniEventRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Alumni event updated",
                eventService.updateEvent(getUserId(userDetails), id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ALUMNI')")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        eventService.deleteEvent(getUserId(userDetails), id);
        return ResponseEntity.ok(ApiResponse.success("Alumni event deleted", null));
    }

    @PostMapping("/{id}/registrations")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<EventRegistrationResponse>> register(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody EventRegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Event registration completed",
                eventService.register(getUserId(userDetails), id, request)));
    }

    @GetMapping("/registrations/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<EventRegistrationResponse>>> getRegistrations(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Event registrations retrieved",
                eventService.getRegistrations(getUserId(userDetails))));
    }

    private Long getUserId(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userDetails.getUsername()));
        return user.getId();
    }
}
