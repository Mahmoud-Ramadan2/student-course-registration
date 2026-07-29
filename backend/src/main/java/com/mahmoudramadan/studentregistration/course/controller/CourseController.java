package com.mahmoudramadan.studentregistration.course.controller;

import com.mahmoudramadan.studentregistration.course.dto.CreateCourseRequest;
import com.mahmoudramadan.studentregistration.course.dto.CourseResponse;
import com.mahmoudramadan.studentregistration.course.dto.UpdateCourseRequest;
import com.mahmoudramadan.studentregistration.course.service.CourseService;
import com.mahmoudramadan.studentregistration.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Courses")
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'REGISTRAR')")
    public ResponseEntity<ApiResponse<CourseResponse>> create(@Valid @RequestBody CreateCourseRequest request) {
        log.debug("Create course code={}", request.code());
        CourseResponse response = courseService.create(request);
        return ResponseEntity.ok(ApiResponse.created(response, "Course created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse>> findById(@PathVariable Long id) {
        log.debug("Find course by id={}", id);
        CourseResponse response = courseService.findById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseResponse>>> findAll() {
        log.debug("Find all courses");
        List<CourseResponse> response = courseService.findAll();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'REGISTRAR')")
    public ResponseEntity<ApiResponse<CourseResponse>> update(
            @PathVariable Long id, @Valid @RequestBody UpdateCourseRequest request) {
        log.debug("Update course id={}", id);
        CourseResponse response = courseService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.debug("Delete course id={}", id);
        courseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
