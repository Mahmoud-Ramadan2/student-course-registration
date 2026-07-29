package com.mahmoudramadan.studentregistration.enrollment.controller;

import com.mahmoudramadan.studentregistration.enrollment.dto.EnrollRequest;
import com.mahmoudramadan.studentregistration.enrollment.dto.EnrollmentResponse;
import com.mahmoudramadan.studentregistration.enrollment.service.EnrollmentService;
import com.mahmoudramadan.studentregistration.infra.security.CustomUserDetails;
import com.mahmoudramadan.studentregistration.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor
@Tag(name = "Enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT', 'REGISTRAR')")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enroll(
            @Valid @RequestBody EnrollRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        EnrollmentResponse response = enrollmentService.enroll(request, currentUser);
        return ResponseEntity.ok(ApiResponse.created(response, "Enrolled successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> findById(@PathVariable Long id) {
        EnrollmentResponse response = enrollmentService.findById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/{id}/drop")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT', 'REGISTRAR')")
    public ResponseEntity<Void> drop(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        enrollmentService.drop(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
