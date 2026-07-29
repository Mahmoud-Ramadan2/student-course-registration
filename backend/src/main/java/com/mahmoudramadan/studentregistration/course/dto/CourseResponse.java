package com.mahmoudramadan.studentregistration.course.dto;

import java.time.Instant;

public record CourseResponse(
        Long id,
        String code,
        String title,
        String description,
        Short creditHours,
        String department,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {}
