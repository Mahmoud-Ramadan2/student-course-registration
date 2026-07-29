package com.mahmoudramadan.studentregistration.course.repo;

import com.mahmoudramadan.studentregistration.course.entity.CourseOffering;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseOfferingRepository extends JpaRepository<CourseOffering, Long> {

    Optional<CourseOffering> findByCourseIdAndTermIdAndSectionNumber(Long courseId, Long termId, String sectionNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM CourseOffering o WHERE o.id = :id")
    Optional<CourseOffering> findByIdForUpdate(@Param("id") Long id);

    @Query("""
    SELECT COUNT(o) > 0
    FROM CourseOffering o
    WHERE o.id = :offeringId
      AND o.instructor.id = :staffUserId
    """)
    boolean isInstructorOfOffering(@Param("offeringId") Long offeringId, @Param("staffUserId") Long staffUserId);

}
