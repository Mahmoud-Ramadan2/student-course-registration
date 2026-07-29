package com.mahmoudramadan.studentregistration.staff.dto;

import jakarta.validation.constraints.Email;

import java.time.LocalDate;

public record UpdateStaffRequest(
        @Email String email,
        String firstName,
        String lastName,
        String title,
        String department,
        LocalDate hireDate
) {}
