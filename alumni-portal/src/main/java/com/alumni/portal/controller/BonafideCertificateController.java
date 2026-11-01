package com.alumni.portal.controller;

import com.alumni.portal.dto.response.ApiResponse;
import com.alumni.portal.dto.response.BonafideCertificateResponse;
import com.alumni.portal.entity.User;
import com.alumni.portal.exception.ResourceNotFoundException;
import com.alumni.portal.repository.UserRepository;
import com.alumni.portal.service.BonafideCertificateService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/alumni/me/bonafide")
@PreAuthorize("hasRole('ALUMNI')")
public class BonafideCertificateController {

    private final BonafideCertificateService certificateService;
    private final UserRepository userRepository;

    public BonafideCertificateController(BonafideCertificateService certificateService,
                                         UserRepository userRepository) {
        this.certificateService = certificateService;
        this.userRepository = userRepository;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<BonafideCertificateResponse>> upload(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success("Bonafide certificate uploaded",
                certificateService.upload(getUserId(userDetails), file)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<BonafideCertificateResponse>> getStatus(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Bonafide certificate status retrieved",
                certificateService.getStatus(getUserId(userDetails))));
    }

    private Long getUserId(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userDetails.getUsername()));
        return user.getId();
    }
}
