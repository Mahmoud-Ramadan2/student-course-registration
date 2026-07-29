package com.mahmoudramadan.studentregistration.student.dto;

import com.mahmoudramadan.studentregistration.student.enums.StudentStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateStudentRequest(
        String username,
        @Email String email,
        String firstName,
        String lastName,
        @Size(min = 8) String password,
        @Past LocalDate dateOfBirth,
        @Pattern(regexp = "^\\+?[0-9]{10,15}$") String phone,
        String address,
        StudentStatus status
) {}
