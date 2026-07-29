package com.mahmoudramadan.studentregistration.staff.dto;

import com.mahmoudramadan.studentregistration.user.enums.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateStaffRequest(
        @NotBlank String username,
        @NotBlank @Size(min = 8) String password,
        @NotBlank @Email String email,
        @NotBlank String employeeNumber,
        @NotBlank String firstName,
        @NotBlank String lastName,
        String title,
        String department,
        LocalDate hireDate,
        @NotNull RoleName role
) {}
