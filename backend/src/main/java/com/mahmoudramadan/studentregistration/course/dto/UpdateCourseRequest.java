package com.mahmoudramadan.studentregistration.course.dto;

public record UpdateCourseRequest(
        String title,
        String description,
        Short creditHours,
        String department,
        Boolean active
) {}
