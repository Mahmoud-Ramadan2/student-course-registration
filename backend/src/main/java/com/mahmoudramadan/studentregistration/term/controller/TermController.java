package com.mahmoudramadan.studentregistration.term.controller;

import com.mahmoudramadan.studentregistration.shared.dto.ApiResponse;
import com.mahmoudramadan.studentregistration.term.dto.CreateTermRequest;
import com.mahmoudramadan.studentregistration.term.dto.TermResponse;
import com.mahmoudramadan.studentregistration.term.dto.UpdateTermRequest;
import com.mahmoudramadan.studentregistration.term.service.TermService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/terms")
@RequiredArgsConstructor
@Tag(name = "Terms")
public class TermController {

    private final TermService termService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'REGISTRAR')")
    public ResponseEntity<ApiResponse<TermResponse>> create(@Valid @RequestBody CreateTermRequest request) {
        TermResponse response = termService.create(request);
        return ResponseEntity.ok(ApiResponse.created(response, "Term created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TermResponse>> findById(@PathVariable Long id) {
        TermResponse response = termService.findById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TermResponse>>> findAll() {
        List<TermResponse> response = termService.findAll();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'REGISTRAR')")
    public ResponseEntity<ApiResponse<TermResponse>> update(
            @PathVariable Long id, @Valid @RequestBody UpdateTermRequest request) {
        TermResponse response = termService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        termService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
