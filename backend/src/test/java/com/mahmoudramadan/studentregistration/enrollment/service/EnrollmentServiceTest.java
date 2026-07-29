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
import com.mahmoudramadan.studentregistration.enrollment.mapper.EnrollmentMapper;
import com.mahmoudramadan.studentregistration.enrollment.repo.EnrollmentRepository;
import com.mahmoudramadan.studentregistration.infra.security.CustomUserDetails;
import com.mahmoudramadan.studentregistration.shared.exception.BusinessException;
import com.mahmoudramadan.studentregistration.shared.exception.ResourceNotFoundException;
import com.mahmoudramadan.studentregistration.student.entity.Student;
import com.mahmoudramadan.studentregistration.student.repo.StudentRepository;
import com.mahmoudramadan.studentregistration.term.entity.Term;
import com.mahmoudramadan.studentregistration.user.entity.Role;
import com.mahmoudramadan.studentregistration.user.entity.User;
import com.mahmoudramadan.studentregistration.user.enums.RoleName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private EnrollmentMapper enrollmentMapper;
    @Mock private CourseOfferingRepository courseOfferingRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private ActivityLogService activityLogService;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private Course course;
    private Term term;
    private CourseOffering offering;
    private Student student;
    private User studentUser;
    private User adminUser;
    private CustomUserDetails studentPrincipal;
    private CustomUserDetails adminPrincipal;

    @BeforeEach
    void setUp() {
        course = Course.builder()
                .code("CS101")
                .title("Intro to CS")
                .creditHours((short) 3)
                .prerequisites(Set.of())
                .build();
        course.setId(10L);

        term = Term.builder()
                .name("Fall 2026")
                .startDate(LocalDate.now().minusDays(10))
                .endDate(LocalDate.now().plusDays(80))
                .registrationStart(Instant.now().minus(5, ChronoUnit.DAYS))
                .registrationEnd(Instant.now().plus(5, ChronoUnit.DAYS))
                .isActive(true)
                .build();
        term.setId(20L);

        offering = CourseOffering.builder()
                .course(course)
                .term(term)
                .capacity(30)
                .waitlistCapacity(5)
                .status(OfferingStatus.OPEN)
                .sectionNumber("001")
                .build();
        offering.setId(30L);

        studentUser = User.builder()
                .username("student1")
                .email("student1@test.com")
                .active(true)
                .build();
        studentUser.setId(1L);
        studentUser.addRole(createRole(RoleName.STUDENT));

        adminUser = User.builder()
                .username("admin1")
                .email("admin1@test.com")
                .active(true)
                .build();
        adminUser.setId(2L);
        adminUser.addRole(createRole(RoleName.ADMIN));

        student = Student.builder()
                .user(studentUser)
                .studentNumber("S001")
                .firstName("John")
                .lastName("Doe")
                .build();
        student.setId(1L);

        studentPrincipal = new CustomUserDetails(studentUser);
        adminPrincipal = new CustomUserDetails(adminUser);
    }

    private Role createRole(RoleName name) {
        Role role = new Role();
        role.setRoleName(name);
        return role;
    }

    @Test
    void enroll_success_enrolled() {
        when(courseOfferingRepository.findByIdForUpdate(30L)).thenReturn(Optional.of(offering));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentIdAndCourseIdAndTermIdAndStatus(
                eq(1L), eq(10L), eq(20L), eq(EnrollmentStatus.ENROLLED)))
                .thenReturn(Optional.empty());
        when(enrollmentRepository.findByStudentIdAndCourseIdAndTermIdAndStatus(
                eq(1L), eq(10L), eq(20L), eq(EnrollmentStatus.WAITLISTED)))
                .thenReturn(Optional.empty());
        when(enrollmentRepository.countByOfferingIdAndStatus(30L, EnrollmentStatus.ENROLLED))
                .thenReturn(5L);

        when(enrollmentRepository.save(any())).thenAnswer(invocation -> {
            Enrollment e = invocation.getArgument(0);
            e.setId(100L);
            return e;
        });

        EnrollmentResponse expected = new EnrollmentResponse(
                100L, 1L, "John Doe", 10L, "CS101", "Intro to CS",
                30L, "001", 20L, "Fall 2026",
                EnrollmentStatus.ENROLLED, null, null, null);
        when(enrollmentMapper.toResponse(any())).thenReturn(expected);

        EnrollRequest request = new EnrollRequest(30L, null);
        EnrollmentResponse result = enrollmentService.enroll(request, studentPrincipal);

        assertThat(result).isEqualTo(expected);
        verify(enrollmentRepository).save(any());
        verify(activityLogService).log(eq("ENROLLMENT_CREATED"), eq("Enrollment"), eq(100L), any());
    }

    @Test
    void enroll_success_waitlisted() {
        when(courseOfferingRepository.findByIdForUpdate(30L)).thenReturn(Optional.of(offering));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentIdAndCourseIdAndTermIdAndStatus(
                any(), any(), any(), eq(EnrollmentStatus.ENROLLED)))
                .thenReturn(Optional.empty());
        when(enrollmentRepository.findByStudentIdAndCourseIdAndTermIdAndStatus(
                any(), any(), any(), eq(EnrollmentStatus.WAITLISTED)))
                .thenReturn(Optional.empty());
        when(enrollmentRepository.countByOfferingIdAndStatus(30L, EnrollmentStatus.ENROLLED))
                .thenReturn(30L);

        when(enrollmentRepository.save(any())).thenAnswer(invocation -> {
            Enrollment e = invocation.getArgument(0);
            e.setId(101L);
            return e;
        });

        EnrollmentResponse expected = new EnrollmentResponse(
                101L, 1L, "John Doe", 10L, "CS101", "Intro to CS",
                30L, "001", 20L, "Fall 2026",
                EnrollmentStatus.WAITLISTED, 1, null, null);
        when(enrollmentMapper.toResponse(any())).thenReturn(expected);

        EnrollRequest request = new EnrollRequest(30L, 1L);
        EnrollmentResponse result = enrollmentService.enroll(request, studentPrincipal);

        assertThat(result).isEqualTo(expected);
        verify(enrollmentRepository).save(any());
    }

    @Test
    void enroll_duplicate_enrolled_throws() {
        when(courseOfferingRepository.findByIdForUpdate(30L)).thenReturn(Optional.of(offering));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentIdAndCourseIdAndTermIdAndStatus(
                eq(1L), eq(10L), eq(20L), eq(EnrollmentStatus.ENROLLED)))
                .thenReturn(Optional.of(Enrollment.builder().build()));

        EnrollRequest request = new EnrollRequest(30L, null);

        assertThatThrownBy(() -> enrollmentService.enroll(request, studentPrincipal))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Already enrolled");
    }

    @Test
    void enroll_courseFull_noWaitlist_throws() {
        offering.setWaitlistCapacity(0);
        when(courseOfferingRepository.findByIdForUpdate(30L)).thenReturn(Optional.of(offering));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentIdAndCourseIdAndTermIdAndStatus(
                any(), any(), any(), eq(EnrollmentStatus.ENROLLED)))
                .thenReturn(Optional.empty());
        when(enrollmentRepository.findByStudentIdAndCourseIdAndTermIdAndStatus(
                any(), any(), any(), eq(EnrollmentStatus.WAITLISTED)))
                .thenReturn(Optional.empty());
        when(enrollmentRepository.countByOfferingIdAndStatus(30L, EnrollmentStatus.ENROLLED))
                .thenReturn(30L);

        EnrollRequest request = new EnrollRequest(30L, null);

        assertThatThrownBy(() -> enrollmentService.enroll(request, studentPrincipal))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Course is full");
    }

    @Test
    void enroll_waitlistFull_throws() {
        when(courseOfferingRepository.findByIdForUpdate(30L)).thenReturn(Optional.of(offering));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentIdAndCourseIdAndTermIdAndStatus(
                any(), any(), any(), eq(EnrollmentStatus.ENROLLED)))
                .thenReturn(Optional.empty());
        when(enrollmentRepository.findByStudentIdAndCourseIdAndTermIdAndStatus(
                any(), any(), any(), eq(EnrollmentStatus.WAITLISTED)))
                .thenReturn(Optional.empty());
        when(enrollmentRepository.countByOfferingIdAndStatus(30L, EnrollmentStatus.ENROLLED))
                .thenReturn(30L);
        when(enrollmentRepository.countByOfferingIdAndStatus(30L, EnrollmentStatus.WAITLISTED))
                .thenReturn(5L);

        EnrollRequest request = new EnrollRequest(30L, null);

        assertThatThrownBy(() -> enrollmentService.enroll(request, studentPrincipal))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("waitlist is also full");
    }

    @Test
    void enroll_prerequisiteNotMet_throws() {
        Course prereq = Course.builder().code("MATH101").title("Calculus").build();
        prereq.setId(99L);
        course.setPrerequisites(Set.of(prereq));

        when(courseOfferingRepository.findByIdForUpdate(30L)).thenReturn(Optional.of(offering));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.existsByStudentIdAndCourseIdAndStatus(
                eq(1L), eq(99L), eq(EnrollmentStatus.COMPLETED)))
                .thenReturn(false);

        EnrollRequest request = new EnrollRequest(30L, null);

        assertThatThrownBy(() -> enrollmentService.enroll(request, studentPrincipal))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("prerequisite");
    }

    @Test
    void enroll_registrationNotOpen_throws() {
        term.setRegistrationStart(Instant.now().plus(1, ChronoUnit.DAYS));

        when(courseOfferingRepository.findByIdForUpdate(30L)).thenReturn(Optional.of(offering));

        EnrollRequest request = new EnrollRequest(30L, null);

        assertThatThrownBy(() -> enrollmentService.enroll(request, studentPrincipal))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("has not opened");
    }

    @Test
    void enroll_registrationClosed_throws() {
        term.setRegistrationEnd(Instant.now().minus(1, ChronoUnit.DAYS));

        when(courseOfferingRepository.findByIdForUpdate(30L)).thenReturn(Optional.of(offering));

        EnrollRequest request = new EnrollRequest(30L, null);

        assertThatThrownBy(() -> enrollmentService.enroll(request, studentPrincipal))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("has closed");
    }

    @Test
    void enroll_offeringNotOpen_throws() {
        offering.setStatus(OfferingStatus.CANCELLED);

        when(courseOfferingRepository.findByIdForUpdate(30L)).thenReturn(Optional.of(offering));

        EnrollRequest request = new EnrollRequest(30L, null);

        assertThatThrownBy(() -> enrollmentService.enroll(request, studentPrincipal))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not available for enrollment");
    }

    @Test
    void enroll_adminEnrollsOtherStudent_success() {
        Student otherStudent = Student.builder().studentNumber("S003").build();
        otherStudent.setId(3L);

        when(courseOfferingRepository.findByIdForUpdate(30L)).thenReturn(Optional.of(offering));
        when(studentRepository.findById(3L)).thenReturn(Optional.of(otherStudent));
        when(enrollmentRepository.findByStudentIdAndCourseIdAndTermIdAndStatus(
                any(), any(), any(), eq(EnrollmentStatus.ENROLLED)))
                .thenReturn(Optional.empty());
        when(enrollmentRepository.findByStudentIdAndCourseIdAndTermIdAndStatus(
                any(), any(), any(), eq(EnrollmentStatus.WAITLISTED)))
                .thenReturn(Optional.empty());
        when(enrollmentRepository.countByOfferingIdAndStatus(30L, EnrollmentStatus.ENROLLED))
                .thenReturn(5L);

        when(enrollmentRepository.save(any())).thenAnswer(invocation -> {
            Enrollment e = invocation.getArgument(0);
            e.setId(102L);
            return e;
        });

        EnrollmentResponse expected = new EnrollmentResponse(
                102L, 3L, null, 10L, "CS101", "Intro to CS",
                30L, "001", 20L, "Fall 2026",
                EnrollmentStatus.ENROLLED, null, null, null);
        when(enrollmentMapper.toResponse(any())).thenReturn(expected);

        EnrollRequest request = new EnrollRequest(30L, 3L);
        EnrollmentResponse result = enrollmentService.enroll(request, adminPrincipal);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void enroll_studentEnrollsOther_denied() {
        when(courseOfferingRepository.findByIdForUpdate(30L)).thenReturn(Optional.of(offering));

        EnrollRequest request = new EnrollRequest(30L, 3L);

        assertThatThrownBy(() -> enrollmentService.enroll(request, studentPrincipal))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("can only enroll yourself");
    }

    @Test
    void enroll_offeringNotFound_throws() {
        when(courseOfferingRepository.findByIdForUpdate(999L)).thenReturn(Optional.empty());

        EnrollRequest request = new EnrollRequest(999L, null);

        assertThatThrownBy(() -> enrollmentService.enroll(request, studentPrincipal))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Course offering not found");
    }

    @Test
    void drop_enrolled_promotesNextWaitlisted() {
        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .offering(offering)
                .course(course)
                .term(term)
                .status(EnrollmentStatus.ENROLLED)
                .build();
        enrollment.setId(100L);

        when(enrollmentRepository.findById(100L)).thenReturn(Optional.of(enrollment));
        when(courseOfferingRepository.findByIdForUpdate(30L)).thenReturn(Optional.of(offering));

        Enrollment waitlisted = Enrollment.builder()
                .student(student)
                .offering(offering)
                .course(course)
                .term(term)
                .status(EnrollmentStatus.WAITLISTED)
                .waitlistPosition(1)
                .build();
        waitlisted.setId(200L);

        when(enrollmentRepository.findByOfferingIdAndStatusOrderByWaitlistPositionAscForUpdate(
                30L, EnrollmentStatus.WAITLISTED))
                .thenReturn(List.of(waitlisted));
        when(enrollmentRepository.findByOfferingIdAndStatusOrderByWaitlistPositionAsc(
                30L, EnrollmentStatus.WAITLISTED))
                .thenReturn(List.of());

        enrollmentService.drop(100L, studentPrincipal);

        ArgumentCaptor<Enrollment> captor = ArgumentCaptor.captor();
        verify(enrollmentRepository, times(2)).save(captor.capture());

        Enrollment dropped = captor.getAllValues().get(0);
        assertThat(dropped.getStatus()).isEqualTo(EnrollmentStatus.DROPPED);
        assertThat(dropped.getDroppedAt()).isNotNull();

        Enrollment promoted = captor.getAllValues().get(1);
        assertThat(promoted.getStatus()).isEqualTo(EnrollmentStatus.ENROLLED);
        assertThat(promoted.getWaitlistPosition()).isNull();

        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void drop_waitlisted_renumbers() {
        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .offering(offering)
                .course(course)
                .term(term)
                .status(EnrollmentStatus.WAITLISTED)
                .waitlistPosition(2)
                .build();
        enrollment.setId(100L);

        when(enrollmentRepository.findById(100L)).thenReturn(Optional.of(enrollment));
        when(courseOfferingRepository.findByIdForUpdate(30L)).thenReturn(Optional.of(offering));

        Enrollment remaining1 = Enrollment.builder()
                .waitlistPosition(1).status(EnrollmentStatus.WAITLISTED).build();
        remaining1.setId(201L);
        Enrollment remaining3 = Enrollment.builder()
                .waitlistPosition(3).status(EnrollmentStatus.WAITLISTED).build();
        remaining3.setId(203L);

        when(enrollmentRepository.findByOfferingIdAndStatusOrderByWaitlistPositionAsc(
                30L, EnrollmentStatus.WAITLISTED))
                .thenReturn(List.of(remaining1, remaining3));

        enrollmentService.drop(100L, studentPrincipal);

        verify(enrollmentRepository).updateWaitlistPosition(201L, 1);
        verify(enrollmentRepository).updateWaitlistPosition(203L, 2);
    }

    @Test
    void drop_otherStudent_denied() {
        Student otherStudent = Student.builder().build();
        otherStudent.setId(3L);
        Enrollment enrollment = Enrollment.builder()
                .student(otherStudent).offering(offering).course(course).term(term)
                .status(EnrollmentStatus.ENROLLED).build();
        enrollment.setId(100L);

        when(enrollmentRepository.findById(100L)).thenReturn(Optional.of(enrollment));
        when(courseOfferingRepository.findByIdForUpdate(30L)).thenReturn(Optional.of(offering));

        assertThatThrownBy(() -> enrollmentService.drop(100L, studentPrincipal))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Cannot drop another student's enrollment");
    }

    @Test
    void drop_afterDeadline_studentDenied() {
        term.setRegistrationEnd(Instant.now().minus(1, ChronoUnit.DAYS));
        Enrollment enrollment = Enrollment.builder()
                .student(student).offering(offering).course(course).term(term)
                .status(EnrollmentStatus.ENROLLED).build();
        enrollment.setId(100L);

        when(enrollmentRepository.findById(100L)).thenReturn(Optional.of(enrollment));

        assertThatThrownBy(() -> enrollmentService.drop(100L, studentPrincipal))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Drop deadline");
    }

    @Test
    void drop_afterDeadline_adminAllowed() {
        term.setRegistrationEnd(Instant.now().minus(1, ChronoUnit.DAYS));
        Enrollment enrollment = Enrollment.builder()
                .student(student).offering(offering).course(course).term(term)
                .status(EnrollmentStatus.ENROLLED).build();
        enrollment.setId(100L);

        when(enrollmentRepository.findById(100L)).thenReturn(Optional.of(enrollment));
        when(courseOfferingRepository.findByIdForUpdate(30L)).thenReturn(Optional.of(offering));
        when(enrollmentRepository.findByOfferingIdAndStatusOrderByWaitlistPositionAscForUpdate(
                30L, EnrollmentStatus.WAITLISTED))
                .thenReturn(List.of());

        enrollmentService.drop(100L, adminPrincipal);

        verify(enrollmentRepository).save(any());
        verify(activityLogService).log(eq("ENROLLMENT_DROPPED"), any(), any(), any());
    }
}
