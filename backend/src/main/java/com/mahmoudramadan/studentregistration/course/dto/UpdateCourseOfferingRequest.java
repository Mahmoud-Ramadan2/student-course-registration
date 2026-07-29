package com.mahmoudramadan.studentregistration.course.dto;

import com.mahmoudramadan.studentregistration.course.enums.OfferingStatus;
import jakarta.validation.constraints.Positive;

import java.time.LocalTime;

public record UpdateCourseOfferingRequest(
        String sectionNumber,
        Long instructorId,
        @Positive Integer capacity,
        @Positive Integer waitlistCapacity,
        String room,
        String daysOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        OfferingStatus status
) {}
