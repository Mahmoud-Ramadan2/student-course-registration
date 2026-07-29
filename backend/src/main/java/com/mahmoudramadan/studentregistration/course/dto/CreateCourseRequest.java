package com.mahmoudramadan.studentregistration.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCourseRequest(
        @NotBlank String code,
        @NotBlank String title,
        String description,
        @NotNull Short creditHours,
        String department
) {}
