package com.mahmoudramadan.studentregistration.enrollment.dto;

import jakarta.validation.constraints.NotNull;

public record EnrollRequest(
        @NotNull Long offeringId,
        Long studentId
) {}
