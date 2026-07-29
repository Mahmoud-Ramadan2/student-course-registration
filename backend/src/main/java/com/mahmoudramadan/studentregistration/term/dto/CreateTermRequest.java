package com.mahmoudramadan.studentregistration.term.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDate;

public record CreateTermRequest(
        @NotBlank String name,
        @NotNull @Future LocalDate startDate,
        @NotNull @Future LocalDate endDate,
        Instant registrationStart,
        Instant registrationEnd,
        boolean active
) {}
