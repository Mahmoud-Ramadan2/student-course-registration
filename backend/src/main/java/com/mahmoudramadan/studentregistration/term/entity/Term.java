package com.mahmoudramadan.studentregistration.term.entity;

import com.mahmoudramadan.studentregistration.shared.entity.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "terms")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Term extends BaseAuditableEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "registration_start")
    private Instant registrationStart;

    @Column(name = "registration_end")
    private Instant registrationEnd;

    @Column(name = "is_active")
    private boolean isActive;
}
