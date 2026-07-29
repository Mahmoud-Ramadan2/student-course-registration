package com.mahmoudramadan.studentregistration.student.controller;

import com.mahmoudramadan.studentregistration.enrollment.dto.EnrollmentResponse;
import com.mahmoudramadan.studentregistration.enrollment.service.EnrollmentService;
import com.mahmoudramadan.studentregistration.infra.security.CustomUserDetails;
import com.mahmoudramadan.studentregistration.shared.dto.ApiResponse;
import com.mahmoudramadan.studentregistration.student.dto.CreateStudentRequest;
import com.mahmoudramadan.studentregistration.student.dto.StudentResponse;
import com.mahmoudramadan.studentregistration.student.dto.UpdateMyProfileRequest;
import com.mahmoudramadan.studentregistration.student.dto.UpdateStudentRequest;
import com.mahmoudramadan.studentregistration.student.service.StudentService;
import org.springframework.security.access.AccessDeniedException;
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
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Students")
public class StudentController {

    private final StudentService studentService;
    private final EnrollmentService enrollmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'REGISTRAR')")
    public ResponseEntity<ApiResponse<StudentResponse>> create(@Valid @RequestBody CreateStudentRequest request) {
        log.debug("Create student username={}", request.username());
        StudentResponse response = studentService.create(request);
        return ResponseEntity.ok(ApiResponse.created(response, "Student created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentResponse>> findById(@PathVariable Long id,
    @AuthenticationPrincipal CustomUserDetails currentUser) {
        log.debug("Find student by id={}", id);
        StudentResponse response = studentService.findById(id, currentUser);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'REGISTRAR', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> findAll() {
        log.debug("Find all students");
        List<StudentResponse> response = studentService.findAll();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'REGISTRAR')")
    public ResponseEntity<ApiResponse<StudentResponse>> update(
            @PathVariable Long id, @Valid @RequestBody UpdateStudentRequest request) {
        log.debug("Update student id={}", id);
        StudentResponse response = studentService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<StudentResponse>> updateMyProfile(
            @Valid @RequestBody UpdateMyProfileRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        log.debug("Update my profile userId={}", currentUser.getId());
        StudentResponse response =
                studentService.updateMyProfile(currentUser.getId(), request);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{studentId}/schedule")
    @PreAuthorize("hasAnyRole('ADMIN','REGISTRAR','STUDENT')")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getSchedule(
            @PathVariable Long studentId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        log.debug("Get schedule for studentId={}", studentId);
        return ResponseEntity.ok(ApiResponse.ok(
                enrollmentService.findStudentSchedule(studentId, currentUser)
        ));
    }
}
