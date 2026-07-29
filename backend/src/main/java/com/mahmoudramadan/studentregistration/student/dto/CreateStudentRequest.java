package com.mahmoudramadan.studentregistration.student.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateStudentRequest(
        @NotBlank String username,
        @NotBlank @Size(min = 8, max = 20) String password,
        @NotBlank @Email String email,
        @NotBlank String studentNumber,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @Past LocalDate dateOfBirth,
        @NotBlank
        @Pattern(regexp = "^\\+?[0-9]{10,15}$")
        String phone,
        String address
) {}
