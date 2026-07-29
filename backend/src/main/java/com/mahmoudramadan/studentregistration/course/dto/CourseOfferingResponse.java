package com.mahmoudramadan.studentregistration.course.dto;

import com.mahmoudramadan.studentregistration.course.enums.OfferingStatus;

import java.time.Instant;
import java.time.LocalTime;

public record CourseOfferingResponse(
        Long id,
        Long courseId,
        String courseCode,
        String courseTitle,
        Long termId,
        String termName,
        String sectionNumber,
        Long instructorId,
        String instructorName,
        Integer capacity,
        Integer waitlistCapacity,
        String room,
        String daysOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        OfferingStatus status,
        Instant createdAt,
        Instant updatedAt
) {}
