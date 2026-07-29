package com.mahmoudramadan.studentregistration.course.entity;

import com.mahmoudramadan.studentregistration.course.enums.OfferingStatus;
import com.mahmoudramadan.studentregistration.shared.entity.BaseAuditableEntity;
import com.mahmoudramadan.studentregistration.staff.entity.Staff;
import com.mahmoudramadan.studentregistration.term.entity.Term;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalTime;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "course_offerings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseOffering extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "term_id")
    private Term term;

    @Column(name = "section_number")
    private String sectionNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id")
    private Staff instructor;

    private Integer capacity;

    @Column(name = "waitlist_capacity")
    private Integer waitlistCapacity;

    private String room;

    @Column(name = "days_of_week")
    private String daysOfWeek;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    private OfferingStatus status;

    @Version
    private Long version;
}
