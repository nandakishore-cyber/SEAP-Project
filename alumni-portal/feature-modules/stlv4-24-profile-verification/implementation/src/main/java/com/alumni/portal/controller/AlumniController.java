package com.alumni.portal.controller;

import com.alumni.portal.dto.request.AlumniProfileRequest;
import com.alumni.portal.dto.request.VerificationRequest;
import com.alumni.portal.dto.response.AlumniProfileResponse;
import com.alumni.portal.dto.response.ApiResponse;
import com.alumni.portal.entity.User;
import com.alumni.portal.entity.enums.VerificationStatus;
import com.alumni.portal.exception.ResourceNotFoundException;
import com.alumni.portal.repository.UserRepository;
import com.alumni.portal.service.AlumniService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for alumni-specific operations.
 * Covers: STLV4-61 (Create Alumni Profile), STLV4-24 (Verification),
 * STLV4-17 (View Alumni Profile + subtasks).
 */
@RestController
@RequestMapping("/api/alumni")
@Tag(name = "Alumni", description = "Alumni profile management and verification endpoints")
public class AlumniController {

    private final AlumniService alumniService;
    private final UserRepository userRepository;

    public AlumniController(AlumniService alumniService, UserRepository userRepository) {
        this.alumniService = alumniService;
        this.userRepository = userRepository;
    }

    /**
     * POST /api/alumni — Create an alumni profile (authenticated users only).
     */
    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create alumni profile")
    public ResponseEntity<ApiResponse<AlumniProfileResponse>> createAlumniProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AlumniProfileRequest request) {
        Long userId = getUserId(userDetails);
        AlumniProfileResponse response = alumniService.createAlumniProfile(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Alumni profile created successfully", response));
    }

    /**
     * GET /api/alumni/me — Get the authenticated user's alumni profile.
     */
    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get own alumni profile")
    public ResponseEntity<ApiResponse<AlumniProfileResponse>> getMyAlumniProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        AlumniProfileResponse response = alumniService.getAlumniProfileByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("Alumni profile retrieved", response));
    }

    /**
     * GET /api/alumni/{id} — View an alumni profile by ID (public).
     */
    @GetMapping("/{id}")
    @Operation(summary = "View alumni profile by ID")
    public ResponseEntity<ApiResponse<AlumniProfileResponse>> getAlumniProfile(
            @PathVariable Long id) {
        AlumniProfileResponse response = alumniService.getAlumniProfileById(id);
        return ResponseEntity.ok(ApiResponse.success("Alumni profile retrieved", response));
    }

    /**
     * GET /api/alumni — Search/list verified alumni profiles (public).
     */
    @GetMapping
    @Operation(summary = "Search alumni profiles")
    public ResponseEntity<ApiResponse<Page<AlumniProfileResponse>>> searchAlumni(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Integer graduationYear,
            @RequestParam(required = false) String company,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<AlumniProfileResponse> results = alumniService.searchAlumni(
                department, graduationYear, company, pageable);
        return ResponseEntity.ok(ApiResponse.success("Alumni profiles retrieved", results));
    }

    /**
     * GET /api/alumni/pending — List pending verification requests (admin only).
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List pending alumni verifications (Admin)")
    public ResponseEntity<ApiResponse<Page<AlumniProfileResponse>>> getPendingVerifications(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<AlumniProfileResponse> results = alumniService.getAlumniByStatus(
                VerificationStatus.PENDING, pageable);
        return ResponseEntity.ok(ApiResponse.success("Pending verifications retrieved", results));
    }

    /**
     * PUT /api/alumni/{id}/verify — Verify or reject an alumni profile (admin only).
     */
    @PutMapping("/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Verify/Reject alumni profile (Admin)")
    public ResponseEntity<ApiResponse<AlumniProfileResponse>> verifyAlumniProfile(
            @PathVariable Long id,
            @Valid @RequestBody VerificationRequest request) {
        AlumniProfileResponse response = alumniService.verifyAlumniProfile(id, request);
        String message = request.getApproved() ? "Alumni profile verified" : "Alumni profile rejected";
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    // ---- Helper ----

    private Long getUserId(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userDetails.getUsername()));
        return user.getId();
    }
}
