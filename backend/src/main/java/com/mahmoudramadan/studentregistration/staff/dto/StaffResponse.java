package com.mahmoudramadan.studentregistration.staff.dto;

import java.time.Instant;
import java.time.LocalDate;

public record StaffResponse(
        Long id,
        String username,
        String email,
        String employeeNumber,
        String firstName,
        String lastName,
        String title,
        String department,
        LocalDate hireDate,
        Instant createdAt,
        Instant updatedAt
) {}
