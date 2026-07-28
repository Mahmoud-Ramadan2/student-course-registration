package com.mahmoudramadan.studentregistration.student.entity;

import com.mahmoudramadan.studentregistration.student.enums.StudentStatus;
import com.mahmoudramadan.studentregistration.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "students")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    @Column(name = "student_number")
    private String studentNumber;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    private String phone;

    private String address;

    @Enumerated(EnumType.STRING)
    private StudentStatus status;
}
