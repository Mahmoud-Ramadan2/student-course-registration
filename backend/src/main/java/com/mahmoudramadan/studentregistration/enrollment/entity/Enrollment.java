package com.mahmoudramadan.studentregistration.enrollment.entity;

import com.mahmoudramadan.studentregistration.course.entity.Course;
import com.mahmoudramadan.studentregistration.course.entity.CourseOffering;
import com.mahmoudramadan.studentregistration.enrollment.enums.EnrollmentStatus;
import com.mahmoudramadan.studentregistration.shared.entity.BaseAuditableEntity;
import com.mahmoudramadan.studentregistration.student.entity.Student;
import com.mahmoudramadan.studentregistration.term.entity.Term;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "enrollments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Enrollment extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offering_id")
    private CourseOffering offering;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "term_id")
    private Term term;

    @Enumerated(EnumType.STRING)
    private EnrollmentStatus status;

    @Column(name = "waitlist_position")
    private Integer waitlistPosition;

    private String grade;

    @Column(name = "enrolled_at")
    private Instant enrolledAt;

    @Column(name = "dropped_at")
    private Instant droppedAt;
}
