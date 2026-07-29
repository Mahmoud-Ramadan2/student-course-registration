package com.mahmoudramadan.studentregistration.term.dto;

import java.time.Instant;
import java.time.LocalDate;

public record UpdateTermRequest(
        String name,
        LocalDate startDate,
        LocalDate endDate,
        Instant registrationStart,
        Instant registrationEnd,
        Boolean active
) {}
