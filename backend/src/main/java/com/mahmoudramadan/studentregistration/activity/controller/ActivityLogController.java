package com.mahmoudramadan.studentregistration.activity.controller;

import com.mahmoudramadan.studentregistration.activity.dto.ActivityLogResponse;
import com.mahmoudramadan.studentregistration.activity.service.ActivityLogService;
import com.mahmoudramadan.studentregistration.shared.dto.ApiResponse;
import com.mahmoudramadan.studentregistration.shared.dto.PagedResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/activity-logs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Activity Logs")
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<ActivityLogResponse>>> findAll(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Long actorId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("Query activity logs entityType={} action={} actorId={}", entityType, action, actorId);
        PagedResponse<ActivityLogResponse> response = activityLogService.findAll(entityType, action, actorId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
