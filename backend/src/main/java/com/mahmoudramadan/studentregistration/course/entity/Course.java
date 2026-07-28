package com.mahmoudramadan.studentregistration.course.entity;

import com.mahmoudramadan.studentregistration.shared.entity.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "courses")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Course extends BaseAuditableEntity {

    private String code;

    private String title;

    private String description;

    @Column(name = "credit_hours")
    private Short creditHours;

    private String department;

    @Column(name = "is_active")
    private boolean isActive;

    @ManyToMany
    @JoinTable(
            name = "course_prerequisites",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "prerequisite_id")
    )
    private Set<Course> prerequisites = new HashSet<>();
}
