package com.mahmoudramadan.studentregistration.course.controller;

import com.mahmoudramadan.studentregistration.course.dto.CourseOfferingResponse;
import com.mahmoudramadan.studentregistration.course.dto.CreateCourseOfferingRequest;
import com.mahmoudramadan.studentregistration.course.dto.UpdateCourseOfferingRequest;
import com.mahmoudramadan.studentregistration.course.service.CourseOfferingService;
import com.mahmoudramadan.studentregistration.enrollment.dto.EnrollmentResponse;
import com.mahmoudramadan.studentregistration.enrollment.service.EnrollmentService;
import com.mahmoudramadan.studentregistration.infra.security.CustomUserDetails;
import com.mahmoudramadan.studentregistration.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/course-offerings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Course Offerings")
public class CourseOfferingController {

    private final CourseOfferingService courseOfferingService;
    private final EnrollmentService enrollmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'REGISTRAR')")
    public ResponseEntity<ApiResponse<CourseOfferingResponse>> create(
            @Valid @RequestBody CreateCourseOfferingRequest request) {
        log.debug("Create course offering courseId={} termId={}", request.courseId(), request.termId());
        CourseOfferingResponse response = courseOfferingService.create(request);
        return ResponseEntity.ok(ApiResponse.created(response, "Course offering created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseOfferingResponse>> findById(@PathVariable Long id) {
        log.debug("Find course offering by id={}", id);
        CourseOfferingResponse response = courseOfferingService.findById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseOfferingResponse>>> findAll() {
        log.debug("Find all course offerings");
        List<CourseOfferingResponse> response = courseOfferingService.findAll();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'REGISTRAR')")
    public ResponseEntity<ApiResponse<CourseOfferingResponse>> update(
            @PathVariable Long id, @Valid @RequestBody UpdateCourseOfferingRequest request) {
        log.debug("Update course offering id={}", id);
        CourseOfferingResponse response = courseOfferingService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.debug("Delete course offering id={}", id);
        courseOfferingService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{offeringId}/enrollments")
    @PreAuthorize("hasAnyRole('ADMIN', 'REGISTRAR', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getEnrollments(
            @PathVariable Long offeringId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        log.debug("Get enrollments for offeringId={}", offeringId);
        List<EnrollmentResponse> response = enrollmentService.findOfferingEnrollments(offeringId, currentUser);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
