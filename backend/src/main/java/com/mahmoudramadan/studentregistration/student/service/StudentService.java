package com.mahmoudramadan.studentregistration.student.service;

import com.mahmoudramadan.studentregistration.activity.service.ActivityLogService;
import com.mahmoudramadan.studentregistration.infra.security.CustomUserDetails;
import com.mahmoudramadan.studentregistration.shared.exception.BusinessException;
import com.mahmoudramadan.studentregistration.shared.exception.ResourceNotFoundException;
import com.mahmoudramadan.studentregistration.student.dto.CreateStudentRequest;
import com.mahmoudramadan.studentregistration.student.dto.StudentResponse;
import com.mahmoudramadan.studentregistration.student.dto.UpdateMyProfileRequest;
import com.mahmoudramadan.studentregistration.student.dto.UpdateStudentRequest;
import com.mahmoudramadan.studentregistration.student.entity.Student;
import com.mahmoudramadan.studentregistration.student.enums.StudentStatus;
import com.mahmoudramadan.studentregistration.student.mapper.StudentMapper;
import com.mahmoudramadan.studentregistration.user.enums.RoleName;
import com.mahmoudramadan.studentregistration.student.repo.StudentRepository;
import com.mahmoudramadan.studentregistration.user.entity.Role;
import com.mahmoudramadan.studentregistration.user.entity.User;
import com.mahmoudramadan.studentregistration.user.repository.RoleRepository;
import com.mahmoudramadan.studentregistration.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StudentMapper studentMapper;
    private final PasswordEncoder passwordEncoder;
    private final ActivityLogService activityLogService;

    @Transactional
    public StudentResponse create(CreateStudentRequest request) {
        if (userRepository.findByUsernameIgnoreCase(request.username()).isPresent()) {
            throw new BusinessException("Username already taken");
        }
        if (userRepository.findByEmailIgnoreCase(request.email()).isPresent()) {
            throw new BusinessException("Email already in use");
        }
        if (studentRepository.findByStudentNumber(request.studentNumber()).isPresent()) {
            throw new BusinessException("Student number already exists");
        }

        Role role = roleRepository.findByRoleName(RoleName.STUDENT)
                .orElseThrow(() -> new BusinessException("Role STUDENT not found"));

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .active(true)
                .build();
        user.addRole(role);
        userRepository.save(user);

        Student student = Student.builder()
                .user(user)
                .studentNumber(request.studentNumber())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .dateOfBirth(request.dateOfBirth())
                .phone(request.phone())
                .address(request.address())
                .status(StudentStatus.ACTIVE)
                .build();
        studentRepository.save(student);

        activityLogService.log("STUDENT_CREATED", "Student", student.getId(),
                Map.of("username", request.username(), "studentNumber", request.studentNumber()));

        return studentMapper.toResponse(student);
    }

    @Transactional(readOnly = true)
    public StudentResponse findById(Long id, CustomUserDetails currentUser) {
        if(currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + RoleName.STUDENT.name()))) {
            if(!currentUser.getId().equals(id)) {
                throw new AccessDeniedException("Students can only access their own records");
            }
        }
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        return studentMapper.toResponse(student);
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> findAll() {
        return studentRepository.findAll().stream()
                .map(studentMapper::toResponse)
                .toList();
    }

    @Transactional
    public StudentResponse update(Long id, UpdateStudentRequest request) {

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        if (request.email() != null && !request.email().equals(student.getUser().getEmail())) {
            if (userRepository.findByEmailIgnoreCase(request.email()).isPresent()) {
                throw new BusinessException("Email already in use");
            }
            student.getUser().setEmail(request.email());
        }
        if (request.firstName() != null && !request.firstName().equals(student.getFirstName())) {
            student.setFirstName(request.firstName());
        }
        if (request.lastName() != null && !request.lastName().equals(student.getLastName())) {
            student.setLastName(request.lastName());
        }
        if (request.dateOfBirth() != null && !request.dateOfBirth().equals(student.getDateOfBirth())) {
            student.setDateOfBirth(request.dateOfBirth());
        }
        if (request.phone() != null && !request.phone().equals(student.getPhone())) {
            student.setPhone(request.phone());
        }
        if (request.address() != null && !request.address().equals(student.getAddress())) {
            student.setAddress(request.address());
        }
        if (request.status() != null && !request.status().equals(student.getStatus())) {
            student.setStatus(request.status());
        }

        studentRepository.save(student);

        activityLogService.log("STUDENT_UPDATED", "Student", id,
              Map.of("studentNumber", student.getStudentNumber()));

        return studentMapper.toResponse(student);
    }

    @Transactional
    public StudentResponse updateMyProfile(Long id, UpdateMyProfileRequest request) {

    Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));


        if (request.phone() != null && !request.phone().equals(student.getPhone())) {
            student.setPhone(request.phone());
        }
        if (request.address() != null && !request.address().equals(student.getAddress())) {
            student.setAddress(request.address());
        }
        if (request.password() != null && !request.password().isBlank()) {
            student.getUser().setPasswordHash(passwordEncoder.encode(request.password()));
        }

        studentRepository.save(student);

        activityLogService.log("STUDENT_PROFILE_UPDATED", "Student", id,
               Map.of("studentNumber", student.getStudentNumber()));

        return studentMapper.toResponse(student);
    }
}
