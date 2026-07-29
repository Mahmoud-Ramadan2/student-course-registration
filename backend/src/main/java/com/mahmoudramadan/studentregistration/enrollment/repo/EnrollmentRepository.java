package com.mahmoudramadan.studentregistration.enrollment.repo;

import com.mahmoudramadan.studentregistration.enrollment.entity.Enrollment;
import com.mahmoudramadan.studentregistration.enrollment.enums.EnrollmentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    long countByOfferingIdAndStatus(Long offeringId, EnrollmentStatus status);

    Optional<Enrollment> findByStudentIdAndCourseIdAndTermIdAndStatus(
            Long studentId, Long courseId, Long termId, EnrollmentStatus status);

    List<Enrollment> findByStudentIdAndStatus(Long studentId, EnrollmentStatus status);

    List<Enrollment> findByOfferingIdAndStatusOrderByWaitlistPositionAsc(Long offeringId, EnrollmentStatus status);

    List<Enrollment> findByOfferingIdOrderByStatusAscWaitlistPositionAsc(Long offeringId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Enrollment e WHERE e.offering.id = :offeringId " +
            "AND e.status = :status ORDER BY e.waitlistPosition ASC")
    List<Enrollment> findByOfferingIdAndStatusOrderByWaitlistPositionAscForUpdate(
            @Param("offeringId") Long offeringId,
            @Param("status") EnrollmentStatus status);

    boolean existsByStudentIdAndCourseIdAndStatus(Long studentId, Long courseId, EnrollmentStatus status);

    @Modifying
    @Query("UPDATE Enrollment e SET e.waitlistPosition = :position " +
            "WHERE e.id = :id")
    void updateWaitlistPosition(@Param("id") Long id, @Param("position") int position);

}
