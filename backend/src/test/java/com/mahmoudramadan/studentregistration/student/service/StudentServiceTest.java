package com.mahmoudramadan.studentregistration.student.service;

import com.mahmoudramadan.studentregistration.activity.service.ActivityLogService;
import com.mahmoudramadan.studentregistration.infra.security.CustomUserDetails;
import com.mahmoudramadan.studentregistration.shared.exception.BusinessException;
import com.mahmoudramadan.studentregistration.shared.exception.ResourceNotFoundException;
import com.mahmoudramadan.studentregistration.student.dto.CreateStudentRequest;
import com.mahmoudramadan.studentregistration.student.dto.StudentResponse;
import com.mahmoudramadan.studentregistration.student.dto.UpdateMyProfileRequest;
import com.mahmoudramadan.studentregistration.student.entity.Student;
import com.mahmoudramadan.studentregistration.student.enums.StudentStatus;
import com.mahmoudramadan.studentregistration.student.mapper.StudentMapper;
import com.mahmoudramadan.studentregistration.student.repo.StudentRepository;
import com.mahmoudramadan.studentregistration.user.entity.Role;
import com.mahmoudramadan.studentregistration.user.entity.User;
import com.mahmoudramadan.studentregistration.user.enums.RoleName;
import com.mahmoudramadan.studentregistration.user.repository.RoleRepository;
import com.mahmoudramadan.studentregistration.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock private StudentRepository studentRepository;
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private StudentMapper studentMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ActivityLogService activityLogService;

    @InjectMocks
    private StudentService studentService;

    private User studentUser;
    private CustomUserDetails studentPrincipal;
    private CustomUserDetails adminPrincipal;

    @BeforeEach
    void setUp() {
        Role studentRole = new Role();
        studentRole.setRoleName(RoleName.STUDENT);

        studentUser = User.builder()
                .username("johndoe")
                .email("john@test.com")
                .passwordHash("encoded-pass")
                .active(true)
                .build();
        studentUser.setId(1L);
        studentUser.addRole(studentRole);

        User otherUser = User.builder().build();
        otherUser.setId(2L);
        otherUser.addRole(studentRole);

        User adminUser = User.builder().build();
        adminUser.setId(99L);
        adminUser.addRole(createRole(RoleName.ADMIN));

        studentPrincipal = new CustomUserDetails(studentUser);
        adminPrincipal = new CustomUserDetails(adminUser);
    }

    private Role createRole(RoleName name) {
        Role role = new Role();
        role.setRoleName(name);
        return role;
    }

    @Test
    void create_success() {
        CreateStudentRequest request = new CreateStudentRequest(
                "johndoe", "password123", "john@test.com",
                "S001", "John", "Doe", null, "01145675347", "123  St");

        when(userRepository.findByUsernameIgnoreCase("johndoe")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("john@test.com")).thenReturn(Optional.empty());
        when(studentRepository.findByStudentNumber("S001")).thenReturn(Optional.empty());

        Role role = createRole(RoleName.STUDENT);
        when(roleRepository.findByRoleName(RoleName.STUDENT)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("password123")).thenReturn("encoded-pass");

        User savedUser = User.builder().username("johndoe").email("john@test.com").build();
        savedUser.setId(1L);
        when(userRepository.save(any())).thenReturn(savedUser);

        when(studentRepository.save(any())).thenAnswer(invocation -> {
            Student s = invocation.getArgument(0);
            s.setId(1L);
            return s;
        });

        StudentResponse expected = new StudentResponse(1L, "johndoe", "john@test.com",
                "S001", "John", "Doe", null, "01145675347", "123 Main St",
                StudentStatus.ACTIVE, null, null);
        when(studentMapper.toResponse(any())).thenReturn(expected);

        StudentResponse result = studentService.create(request);

        assertThat(result).isEqualTo(expected);
        verify(activityLogService).log(eq("STUDENT_CREATED"), eq("Student"), eq(1L), any());
    }

    @Test
    void create_duplicateUsername_throws() {
        CreateStudentRequest request = new CreateStudentRequest(
                "existing", "password123", "john@test.com",
                "S001", "John", "Doe", null, "01145675347", null);

        when(userRepository.findByUsernameIgnoreCase("existing")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> studentService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Username already taken");
    }

    @Test
    void create_duplicateEmail_throws() {
        CreateStudentRequest request = new CreateStudentRequest(
                "johndoe", "password123", "taken@test.com",
                "S001", "John", "Doe", null, "01145675347", null);

        when(userRepository.findByUsernameIgnoreCase("johndoe")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("taken@test.com")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> studentService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Email already in use");
    }

    @Test
    void create_duplicateStudentNumber_throws() {
        CreateStudentRequest request = new CreateStudentRequest(
                "johndoe", "password123", "john@test.com",
                "S001", "John", "Doe", null, "01145675347", null);

        when(userRepository.findByUsernameIgnoreCase("johndoe")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("john@test.com")).thenReturn(Optional.empty());
        when(studentRepository.findByStudentNumber("S001")).thenReturn(Optional.of(new Student()));

        assertThatThrownBy(() -> studentService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Student number already exists");
    }

    @Test
    void findById_selfAccess_success() {
        Student student = Student.builder()
                .user(studentUser).studentNumber("S001")
                .firstName("John").lastName("Doe").build();
        student.setId(1L);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        StudentResponse expected = new StudentResponse(1L, "johndoe", "john@test.com",
                "S001", "John", "Doe", null, "01145675347", null,
                StudentStatus.ACTIVE, null, null);
        when(studentMapper.toResponse(any())).thenReturn(expected);

        StudentResponse result = studentService.findById(1L, studentPrincipal);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void findById_otherStudent_throws() {
        assertThatThrownBy(() -> studentService.findById(2L, studentPrincipal))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("can only access their own records");
    }

    @Test
    void findById_adminAccessAny_success() {
        User otherUser = User.builder().username("other").email("o@t.com").build();
        otherUser.setId(2L);
        Student student = Student.builder()
                .user(otherUser)
                .firstName("Other").lastName("User").build();
        student.setId(2L);

        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));
        StudentResponse expected = new StudentResponse(2L, "other", "o@t.com",
                null, "Other", "User", null, null, null,
                StudentStatus.ACTIVE, null, null);
        when(studentMapper.toResponse(any())).thenReturn(expected);

        StudentResponse result = studentService.findById(2L, adminPrincipal);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void findById_notFound_throws() {
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.findById(999L, adminPrincipal))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Student not found");
    }

    @Test
    void updateMyProfile_updatesPhoneAndPassword() {
        Student student = Student.builder()
                .user(studentUser).studentNumber("S001")
                .firstName("John").lastName("Doe")
                .phone("01145675347").address("123 Main St")
                .build();
        student.setId(1L);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(passwordEncoder.encode("new-password")).thenReturn("new-encoded-pass");

        StudentResponse expected = new StudentResponse(1L, "johndoe", "john@test.com",
                "S001", "John", "Doe", null, "01145675347", "456 Oak Ave",
                StudentStatus.ACTIVE, null, null);
        when(studentMapper.toResponse(any())).thenReturn(expected);
        when(studentRepository.save(any())).thenReturn(student);

        UpdateMyProfileRequest request = new UpdateMyProfileRequest("new-password", "01145675347", "456 Oak Ave");

        StudentResponse result = studentService.updateMyProfile(1L, request);

        assertThat(result).isEqualTo(expected);
        assertThat(studentUser.getPasswordHash()).isEqualTo("new-encoded-pass");
        verify(activityLogService).log(eq("STUDENT_PROFILE_UPDATED"), eq("Student"), eq(1L), any());
    }

    @Test
    void updateMyProfile_onlyPhoneChange() {
        Student student = Student.builder()
                .user(studentUser).studentNumber("S001")
                .firstName("John").lastName("Doe")
                .phone("01145675347").address("123 Main St")
                .build();
        student.setId(1L);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        StudentResponse expected = new StudentResponse(1L, "johndoe", "john@test.com",
                "S001", "John", "Doe", null, "01145675347", "123 Main St",
                StudentStatus.ACTIVE, null, null);
        when(studentMapper.toResponse(any())).thenReturn(expected);
        when(studentRepository.save(any())).thenReturn(student);

        UpdateMyProfileRequest request = new UpdateMyProfileRequest(null, "01145675347", null);

        StudentResponse result = studentService.updateMyProfile(1L, request);

        assertThat(result).isEqualTo(expected);
        assertThat(studentUser.getPasswordHash()).isEqualTo("encoded-pass");
    }
}
