package com.mahmoudramadan.studentregistration.enrollment.dto;

import com.mahmoudramadan.studentregistration.enrollment.enums.EnrollmentStatus;

import java.time.Instant;

public record EnrollmentResponse(
        Long id,
        Long studentId,
        String studentName,
        Long courseId,
        String courseCode,
        String courseTitle,
        Long offeringId,
        String sectionNumber,
        Long termId,
        String termName,
        EnrollmentStatus status,
        Integer waitlistPosition,
        Instant enrolledAt,
        Instant droppedAt
) {}
