package com.mahmoudramadan.studentregistration.activity.dto;

import java.time.Instant;
import java.util.Map;

public record ActivityLogResponse(
        Long id,
        Long actorId,
        String actorUsername,
        String action,
        String entityType,
        Long entityId,
        Map<String, Object> details,
        Instant createdAt
) {}
