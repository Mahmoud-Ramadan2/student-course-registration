package com.mahmoudramadan.studentregistration.student.repo;

import com.mahmoudramadan.studentregistration.student.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByStudentNumber(String studentNumber);

    Optional<Student> findByUserId(Long userId);
}
