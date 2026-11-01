package com.alumni.portal.controller;

import com.alumni.portal.dto.response.ApiResponse;
import com.alumni.portal.dto.response.NotificationResponse;
import com.alumni.portal.dto.request.NotificationSendRequest;
import com.alumni.portal.dto.response.UserSummaryResponse;
import com.alumni.portal.entity.enums.Role;
import com.alumni.portal.entity.User;
import com.alumni.portal.exception.ResourceNotFoundException;
import com.alumni.portal.repository.UserRepository;
import com.alumni.portal.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public NotificationController(NotificationService notificationService, UserRepository userRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved",
                notificationService.getNotifications(getUserId(userDetails))));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Unread notification count retrieved",
                notificationService.getUnreadCount(getUserId(userDetails))));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markRead(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read",
                notificationService.markRead(getUserId(userDetails), id)));
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllRead(
            @AuthenticationPrincipal UserDetails userDetails) {
        notificationService.markAllRead(getUserId(userDetails));
        return ResponseEntity.ok(ApiResponse.success("Notifications marked as read", null));
    }

    @GetMapping("/students")
    @PreAuthorize("hasAnyRole('ALUMNI', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<UserSummaryResponse>>> getStudents() {
        return ResponseEntity.ok(ApiResponse.success("Students retrieved",
                userRepository.findByRole(Role.ROLE_STUDENT).stream()
                        .map(student -> UserSummaryResponse.builder()
                                .id(student.getId())
                                .name(student.getFirstName() + " " + student.getLastName())
                                .email(student.getEmail())
                                .build())
                        .toList()));
    }

    @PostMapping("/send-to-student")
    @PreAuthorize("hasAnyRole('ALUMNI', 'ADMIN')")
    public ResponseEntity<ApiResponse<NotificationResponse>> sendToStudent(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody NotificationSendRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Notification sent to student",
                notificationService.sendToStudent(getUserId(userDetails), request.getStudentId(),
                        request.getTitle(), request.getMessage())));
    }

    private Long getUserId(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userDetails.getUsername()));
        return user.getId();
    }
}
