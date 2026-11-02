package com.alumni.portal.controller;

import com.alumni.portal.dto.request.ConnectionRequestCreateRequest;
import com.alumni.portal.dto.response.ApiResponse;
import com.alumni.portal.dto.response.ConnectionRequestResponse;
import com.alumni.portal.entity.User;
import com.alumni.portal.exception.ResourceNotFoundException;
import com.alumni.portal.repository.UserRepository;
import com.alumni.portal.service.ConnectionRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/connections")
@Tag(name = "Connections", description = "Alumni connection request endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ConnectionRequestController {

    private final ConnectionRequestService connectionRequestService;
    private final UserRepository userRepository;

    public ConnectionRequestController(ConnectionRequestService connectionRequestService, UserRepository userRepository) {
        this.connectionRequestService = connectionRequestService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @Operation(summary = "Send a connection request")
    public ResponseEntity<ApiResponse<ConnectionRequestResponse>> sendRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ConnectionRequestCreateRequest request) {
        Long senderId = getUserId(userDetails);
        ConnectionRequestResponse response = connectionRequestService.sendRequest(senderId, request.getRecipientId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Connection request sent successfully", response));
    }

    @GetMapping("/sent")
    @Operation(summary = "View sent connection requests")
    public ResponseEntity<ApiResponse<List<ConnectionRequestResponse>>> getSentRequests(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        List<ConnectionRequestResponse> response = connectionRequestService.getSentRequests(userId);
        return ResponseEntity.ok(ApiResponse.success("Sent requests retrieved", response));
    }

    @GetMapping("/received")
    @Operation(summary = "View received connection requests")
    public ResponseEntity<ApiResponse<List<ConnectionRequestResponse>>> getReceivedRequests(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        List<ConnectionRequestResponse> response = connectionRequestService.getReceivedRequests(userId);
        return ResponseEntity.ok(ApiResponse.success("Received requests retrieved", response));
    }

    @PutMapping("/{id}/accept")
    @Operation(summary = "Accept a connection request")
    public ResponseEntity<ApiResponse<ConnectionRequestResponse>> acceptRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Long currentUserId = getUserId(userDetails);
        ConnectionRequestResponse response = connectionRequestService.acceptRequest(currentUserId, id);
        return ResponseEntity.ok(ApiResponse.success("Connection request accepted", response));
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject a connection request")
    public ResponseEntity<ApiResponse<ConnectionRequestResponse>> rejectRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Long currentUserId = getUserId(userDetails);
        ConnectionRequestResponse response = connectionRequestService.rejectRequest(currentUserId, id);
        return ResponseEntity.ok(ApiResponse.success("Connection request rejected", response));
    }

    private Long getUserId(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userDetails.getUsername()));
        return user.getId();
    }
}
