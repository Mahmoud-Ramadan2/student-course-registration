package com.mahmoudramadan.studentregistration.course.repo;

import com.mahmoudramadan.studentregistration.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    java.util.Optional<Course> findByCode(String code);
}
