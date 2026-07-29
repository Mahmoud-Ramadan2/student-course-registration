package com.mahmoudramadan.studentregistration.student.dto;

import com.mahmoudramadan.studentregistration.student.enums.StudentStatus;

import java.time.Instant;
import java.time.LocalDate;

public record StudentResponse(
        Long id,
        String username,
        String email,
        String studentNumber,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String phone,
        String address,
        StudentStatus status,
        Instant createdAt,
        Instant updatedAt
) {}
