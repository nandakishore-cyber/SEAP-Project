package com.alumni.portal.controller;
import com.alumni.portal.dto.request.SuccessStoryRequest;
import com.alumni.portal.dto.response.*;
import com.alumni.portal.entity.User;
import com.alumni.portal.exception.ResourceNotFoundException;
import com.alumni.portal.repository.UserRepository;
import com.alumni.portal.service.SuccessStoryService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/stories")
public class SuccessStoryController {
    private final SuccessStoryService service; private final UserRepository users;
    public SuccessStoryController(SuccessStoryService service,UserRepository users) { this.service=service; this.users=users; }
    @GetMapping public ResponseEntity<ApiResponse<List<SuccessStoryResponse>>> published() { return ResponseEntity.ok(ApiResponse.success("Published success stories",service.published())); }
    @GetMapping("/pending") @PreAuthorize("hasRole('ADMIN')") public ResponseEntity<ApiResponse<List<SuccessStoryResponse>>> pending() { return ResponseEntity.ok(ApiResponse.success("Stories awaiting review",service.pending())); }
    @GetMapping("/me") @PreAuthorize("hasRole('ALUMNI')") public ResponseEntity<ApiResponse<List<SuccessStoryResponse>>> mine(@AuthenticationPrincipal UserDetails p) { return ResponseEntity.ok(ApiResponse.success("Your stories",service.mine(userId(p)))); }
    @PostMapping @PreAuthorize("hasRole('ALUMNI')") public ResponseEntity<ApiResponse<SuccessStoryResponse>> create(@AuthenticationPrincipal UserDetails p,@Valid @RequestBody SuccessStoryRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Story submitted for review",service.create(userId(p),request))); }
    @PutMapping("/{id}") @PreAuthorize("hasRole('ALUMNI')") public ResponseEntity<ApiResponse<SuccessStoryResponse>> update(@AuthenticationPrincipal UserDetails p,@PathVariable Long id,@Valid @RequestBody SuccessStoryRequest request) { return ResponseEntity.ok(ApiResponse.success("Pending story updated",service.update(userId(p),id,request))); }
    @PutMapping("/{id}/moderation") @PreAuthorize("hasRole('ADMIN')") public ResponseEntity<ApiResponse<SuccessStoryResponse>> moderate(@PathVariable Long id,@RequestParam String status) { return ResponseEntity.ok(ApiResponse.success("Story moderation updated",service.moderate(id,status.toUpperCase()))); }
    @DeleteMapping("/{id}") public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id,@AuthenticationPrincipal UserDetails p) {
        User u=users.findByEmail(p.getUsername()).orElseThrow(() -> new ResourceNotFoundException("User","email",p.getUsername()));
        service.delete(id,u.getId(),u.getRole().name().equals("ROLE_ADMIN")); return ResponseEntity.ok(ApiResponse.success("Story deleted"));
    }
    private Long userId(UserDetails p) { User u=users.findByEmail(p.getUsername()).orElseThrow(() -> new ResourceNotFoundException("User","email",p.getUsername())); return u.getId(); }
}
