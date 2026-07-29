package com.mahmoudramadan.studentregistration.staff.controller;

import com.mahmoudramadan.studentregistration.shared.dto.ApiResponse;
import com.mahmoudramadan.studentregistration.staff.dto.CreateStaffRequest;
import com.mahmoudramadan.studentregistration.staff.dto.StaffResponse;
import com.mahmoudramadan.studentregistration.staff.dto.UpdateStaffRequest;
import com.mahmoudramadan.studentregistration.staff.service.StaffService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/staff")
@RequiredArgsConstructor
@Tag(name = "Staff")
public class StaffController {

    private final StaffService staffService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StaffResponse>> create(@Valid @RequestBody CreateStaffRequest request) {
        StaffResponse response = staffService.create(request);
        return ResponseEntity.ok(ApiResponse.created(response, "Staff created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StaffResponse>> findById(@PathVariable Long id) {
        StaffResponse response = staffService.findById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<StaffResponse>>> findAll() {
        List<StaffResponse> response = staffService.findAll();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StaffResponse>> update(
            @PathVariable Long id, @Valid @RequestBody UpdateStaffRequest request) {
        StaffResponse response = staffService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
