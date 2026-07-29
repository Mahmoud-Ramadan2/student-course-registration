package com.mahmoudramadan.studentregistration.student.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateMyProfileRequest(
        @Size(min = 8) String password,
        @Pattern(regexp = "^\\+?[0-9]{10,15}$") String phone,
        String address
) {}
