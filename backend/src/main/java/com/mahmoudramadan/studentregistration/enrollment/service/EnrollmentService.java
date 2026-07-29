package com.mahmoudramadan.studentregistration.enrollment.service;

import com.mahmoudramadan.studentregistration.activity.service.ActivityLogService;
import com.mahmoudramadan.studentregistration.course.entity.Course;
import com.mahmoudramadan.studentregistration.course.entity.CourseOffering;
import com.mahmoudramadan.studentregistration.course.enums.OfferingStatus;
import com.mahmoudramadan.studentregistration.course.repo.CourseOfferingRepository;
import com.mahmoudramadan.studentregistration.enrollment.dto.EnrollRequest;
import com.mahmoudramadan.studentregistration.enrollment.dto.EnrollmentResponse;
import com.mahmoudramadan.studentregistration.enrollment.entity.Enrollment;
import com.mahmoudramadan.studentregistration.enrollment.enums.EnrollmentStatus;
import com.mahmoudramadan.studentregistration.enrollment.event.WaitlistPromotedEvent;
import com.mahmoudramadan.studentregistration.enrollment.mapper.EnrollmentMapper;
import com.mahmoudramadan.studentregistration.enrollment.repo.EnrollmentRepository;
import com.mahmoudramadan.studentregistration.infra.security.CustomUserDetails;
import com.mahmoudramadan.studentregistration.shared.exception.BusinessException;
import org.springframework.security.access.AccessDeniedException;
import com.mahmoudramadan.studentregistration.shared.exception.ResourceNotFoundException;
import com.mahmoudramadan.studentregistration.student.entity.Student;
import com.mahmoudramadan.studentregistration.student.repo.StudentRepository;
import com.mahmoudramadan.studentregistration.term.entity.Term;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentMapper enrollmentMapper;
    private final CourseOfferingRepository courseOfferingRepository;
    private final StudentRepository studentRepository;
    private final ActivityLogService activityLogService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public EnrollmentResponse enroll(EnrollRequest request, CustomUserDetails currentUser) {

        CourseOffering offering = courseOfferingRepository.findByIdForUpdate(request.offeringId())
                .orElseThrow(() -> new ResourceNotFoundException("Course offering not found"));

        if (offering.getStatus() != OfferingStatus.OPEN && offering.getStatus() != OfferingStatus.SCHEDULED) {
            throw new BusinessException(
                    "Course offering is not available for enrollment. Current status: " + offering.getStatus());
        }

        // Validate registration window
        Term term = offering.getTerm();
        Instant now = Instant.now();
        if (term.getRegistrationStart() != null && now.isBefore(term.getRegistrationStart())) {
            throw new BusinessException("Registration has not opened yet for this term");
        }
        if (term.getRegistrationEnd() != null && now.isAfter(term.getRegistrationEnd())) {
            throw new BusinessException("Registration has closed for this term");
        }

        Student student;
        if (request.studentId() != null) {
            boolean isAdminOrRegistrar = currentUser.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                                   a.getAuthority().equals("ROLE_REGISTRAR"));

            if (!isAdminOrRegistrar && !request.studentId().equals(currentUser.getId())) {
                throw new AccessDeniedException("You can only enroll yourself");
            }
            student = studentRepository.findById(request.studentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        } else {
            student = studentRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new BusinessException("Authenticated user is not a student"));
        }

        Set<Course> prerequisites = offering.getCourse().getPrerequisites();
        for (Course prereq : prerequisites) {
            boolean completed = enrollmentRepository.existsByStudentIdAndCourseIdAndStatus(
                    student.getId(), prereq.getId(), EnrollmentStatus.COMPLETED);
            if (!completed) {
                throw new BusinessException(
                        "Student has not completed prerequisite: " + prereq.getCode() + " - " + prereq.getTitle());
            }
        }

        enrollmentRepository.findByStudentIdAndCourseIdAndTermIdAndStatus(
                student.getId(), offering.getCourse().getId(), offering.getTerm().getId(), EnrollmentStatus.ENROLLED)
                .ifPresent(e -> { throw new BusinessException("Already enrolled in this course this term"); });

        enrollmentRepository.findByStudentIdAndCourseIdAndTermIdAndStatus(
                student.getId(), offering.getCourse().getId(), offering.getTerm().getId(), EnrollmentStatus.WAITLISTED)
                .ifPresent(e -> { throw new BusinessException("Already waitlisted for this course this term"); });

        long enrolledCount = enrollmentRepository.countByOfferingIdAndStatus(offering.getId(), EnrollmentStatus.ENROLLED);

        Enrollment enrollment;
        if (enrolledCount < offering.getCapacity()) {
            enrollment = Enrollment.builder()
                    .student(student)
                    .offering(offering)
                    .course(offering.getCourse())
                    .term(offering.getTerm())
                    .status(EnrollmentStatus.ENROLLED)
                    .enrolledAt(Instant.now())
                    .build();
        } else if (offering.getWaitlistCapacity() != null && offering.getWaitlistCapacity() > 0) {
            long waitlistedCount = enrollmentRepository.countByOfferingIdAndStatus(offering.getId(), EnrollmentStatus.WAITLISTED);
            if (waitlistedCount >= offering.getWaitlistCapacity()) {
                throw new BusinessException("Course is full and waitlist is also full");
            }
            enrollment = Enrollment.builder()
                    .student(student)
                    .offering(offering)
                    .course(offering.getCourse())
                    .term(offering.getTerm())
                    .status(EnrollmentStatus.WAITLISTED)
                    .waitlistPosition((int) waitlistedCount + 1)
                    .enrolledAt(Instant.now())
                    .build();
        } else {
            throw new BusinessException("Course is full");
        }

        enrollmentRepository.save(enrollment);

        activityLogService.log("ENROLLMENT_CREATED", "Enrollment", enrollment.getId(),
                java.util.Map.of("studentId", student.getId(), "offeringId", offering.getId(),
                        "status", enrollment.getStatus().name()));

        return enrollmentMapper.toResponse(enrollment);
    }

    @Transactional
    public void drop(Long enrollmentId, CustomUserDetails currentUser) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        Term term = enrollment.getTerm();
        Instant now = Instant.now();
        if (term.getRegistrationEnd() != null && now.isAfter(term.getRegistrationEnd())) {
            // Allow admins and registrars to override
            boolean isAdminOrRegistrar = currentUser.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_REGISTRAR") || a.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdminOrRegistrar) {
                throw new BusinessException("Drop deadline has passed for this term");
            }
        }

        courseOfferingRepository.findByIdForUpdate(enrollment.getOffering().getId()); // Lock the offering to prevent race conditions

        if (currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"))) {

            if (!enrollment.getStudent().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("Cannot drop another student's enrollment");
            }
        }

        EnrollmentStatus previousStatus = enrollment.getStatus();
        Long offeringId = enrollment.getOffering().getId();

        enrollment.setStatus(EnrollmentStatus.DROPPED);
        enrollment.setDroppedAt(Instant.now());
        enrollmentRepository.save(enrollment);

        if (previousStatus == EnrollmentStatus.ENROLLED) {
            promoteNextWaitlisted(offeringId);
        } else if (previousStatus == EnrollmentStatus.WAITLISTED) {
            renumberWaitlist(offeringId);
        }

        activityLogService.log("ENROLLMENT_DROPPED", "Enrollment", enrollmentId,
                java.util.Map.of("studentId", enrollment.getStudent().getId(),
                        "offeringId", offeringId));
    }

    @Transactional(readOnly = true)
    public EnrollmentResponse findById(Long id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));
        return enrollmentMapper.toResponse(enrollment);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> findStudentSchedule(
            Long studentId,
            CustomUserDetails currentUser) {

        boolean isAdminOrRegistrar = currentUser.getAuthorities().stream()
                .anyMatch(a ->
                        a.getAuthority().equals("ROLE_ADMIN") ||
                                a.getAuthority().equals("ROLE_REGISTRAR"));

        if (!isAdminOrRegistrar && !currentUser.getId().equals(studentId)) {
            throw new AccessDeniedException("You can only view your own schedule.");
        }

        return enrollmentRepository.findByStudentIdAndStatus(
                        studentId,
                        EnrollmentStatus.ENROLLED)
                .stream()
                .map(enrollmentMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> findOfferingEnrollments(Long offeringId, CustomUserDetails currentUser) {
        boolean isAdminOrRegistrar = currentUser.getAuthorities().stream()
                .anyMatch(a ->
                        a.getAuthority().equals("ROLE_ADMIN") ||
                                a.getAuthority().equals("ROLE_REGISTRAR"));

        if (!isAdminOrRegistrar) {
            CourseOffering offering = courseOfferingRepository.findById(offeringId)
                    .orElseThrow(() -> new ResourceNotFoundException("Course offering not found"));
            if (offering.getInstructor() == null || !offering.getInstructor().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("You can only view enrollments for your own offerings");
            }
        }

        return enrollmentRepository.findByOfferingIdOrderByStatusAscWaitlistPositionAsc(offeringId)
                .stream()
                .map(enrollmentMapper::toResponse)
                .toList();
    }

    private void promoteNextWaitlisted(Long offeringId) {

        enrollmentRepository.findByOfferingIdAndStatusOrderByWaitlistPositionAscForUpdate(
                        offeringId, EnrollmentStatus.WAITLISTED)
                .stream()
                .findFirst()
                .ifPresent(waitlisted -> {
                    waitlisted.setStatus(EnrollmentStatus.ENROLLED);
                    waitlisted.setWaitlistPosition(null);
                    waitlisted.setEnrolledAt(Instant.now());
                    enrollmentRepository.save(waitlisted);

                    renumberWaitlist(offeringId);

                    // Publish event → Send email notification to the student
                    eventPublisher.publishEvent(new WaitlistPromotedEvent(
                            this,
                            waitlisted.getStudent().getUser().getEmail(),
                            waitlisted.getStudent().getUser().getUsername(),
                            waitlisted.getCourse().getTitle(),
                            waitlisted.getTerm().getName()
                    ));

                    activityLogService.log("WAITLIST_PROMOTED", "Enrollment", waitlisted.getId(),
                           Map.of("offeringId", offeringId));
                });
    }

    private void renumberWaitlist(Long offeringId) {
        List<Enrollment> waitlisted = enrollmentRepository
                .findByOfferingIdAndStatusOrderByWaitlistPositionAsc(offeringId, EnrollmentStatus.WAITLISTED);
        int pos = 1;
        for (Enrollment e : waitlisted) {
                enrollmentRepository.updateWaitlistPosition(e.getId(), pos++);
        }
    }
}
