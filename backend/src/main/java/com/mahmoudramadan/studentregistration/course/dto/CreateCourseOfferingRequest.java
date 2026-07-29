package com.mahmoudramadan.studentregistration.course.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalTime;

public record CreateCourseOfferingRequest(
        @NotNull Long courseId,
        @NotNull Long termId,
        String sectionNumber,
        Long instructorId,
        @NotNull @Positive Integer capacity,
        Integer waitlistCapacity,
        String room,
        String daysOfWeek,
        LocalTime startTime,
        LocalTime endTime
) {}
