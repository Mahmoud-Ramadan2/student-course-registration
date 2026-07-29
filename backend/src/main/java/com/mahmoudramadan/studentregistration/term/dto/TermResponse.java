package com.mahmoudramadan.studentregistration.term.dto;

import java.time.Instant;
import java.time.LocalDate;

public record TermResponse(
        Long id,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        Instant registrationStart,
        Instant registrationEnd,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {}
