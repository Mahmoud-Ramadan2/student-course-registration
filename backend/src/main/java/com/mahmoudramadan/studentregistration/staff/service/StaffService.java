package com.mahmoudramadan.studentregistration.staff.service;

import com.mahmoudramadan.studentregistration.activity.service.ActivityLogService;
import com.mahmoudramadan.studentregistration.shared.exception.BusinessException;
import com.mahmoudramadan.studentregistration.shared.exception.ResourceNotFoundException;
import com.mahmoudramadan.studentregistration.staff.dto.CreateStaffRequest;
import com.mahmoudramadan.studentregistration.staff.dto.StaffResponse;
import com.mahmoudramadan.studentregistration.staff.dto.UpdateStaffRequest;
import com.mahmoudramadan.studentregistration.staff.entity.Staff;
import com.mahmoudramadan.studentregistration.staff.mapper.StaffMapper;
import com.mahmoudramadan.studentregistration.staff.repo.StaffRepository;
import com.mahmoudramadan.studentregistration.user.entity.Role;
import com.mahmoudramadan.studentregistration.user.entity.User;
import com.mahmoudramadan.studentregistration.user.repository.RoleRepository;
import com.mahmoudramadan.studentregistration.user.repository.UserRepository;
import com.mahmoudramadan.studentregistration.user.enums.RoleName;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StaffService {

    private final StaffRepository staffRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StaffMapper staffMapper;
    private final PasswordEncoder passwordEncoder;
    private final ActivityLogService activityLogService;

    @Transactional
    public StaffResponse create(CreateStaffRequest request) {
        if (userRepository.findByUsernameIgnoreCase(request.username()).isPresent()) {
            throw new BusinessException("Username already taken");
        }
        if (userRepository.findByEmailIgnoreCase(request.email()).isPresent()) {
            throw new BusinessException("Email already in use");
        }
        if (staffRepository.findByEmployeeNumber(request.employeeNumber()).isPresent()) {
            throw new BusinessException("Employee number already exists");
        }

        Role role = roleRepository.findByRoleName(request.role())
                .orElseThrow(() -> new BusinessException("Role " + request.role() + " not found"));

        if (role.getRoleName().equals(RoleName.STUDENT)) {
            throw new BusinessException("Role " + request.role() + " cannot be assigned to a staff member");
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .active(true)
                .build();
        user.addRole(role);
        userRepository.save(user);

        Staff staff = Staff.builder()
                .user(user)
                .employeeNumber(request.employeeNumber())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .title(request.title())
                .department(request.department())
                .hireDate(request.hireDate())
                .build();
        staffRepository.save(staff);

        activityLogService.log("STAFF_CREATED", "Staff", staff.getId(),
                Map.of("username", request.username(), "role", request.role().name()));

        return staffMapper.toResponse(staff);
    }

    @Transactional(readOnly = true)
    public StaffResponse findById(Long id) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));
        return staffMapper.toResponse(staff);
    }

    @Transactional(readOnly = true)
    public List<StaffResponse> findAll() {
        return staffRepository.findAll().stream()
                .map(staffMapper::toResponse)
                .toList();
    }

    @Transactional
    public StaffResponse update(Long id, UpdateStaffRequest request) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));

        if (request.email() != null && !request.email().equals(staff.getUser().getEmail())) {
            if (userRepository.findByEmailIgnoreCase(request.email()).isPresent()) {
                throw new BusinessException("Email already in use");
            }
            staff.getUser().setEmail(request.email());
        }
        if (request.firstName() != null && !request.firstName().equals(staff.getFirstName())) {
            staff.setFirstName(request.firstName());
        }
        if (request.lastName() != null && !request.lastName().equals(staff.getLastName())) {
            staff.setLastName(request.lastName());
        }
        if (request.title() != null && !request.title().equals(staff.getTitle())) {
            staff.setTitle(request.title());
        }
        if (request.department() != null && !request.department().equals(staff.getDepartment())) {
            staff.setDepartment(request.department());
        }
        if (request.hireDate() != null && !request.hireDate().equals(staff.getHireDate())) {
            staff.setHireDate(request.hireDate());
        }

        staffRepository.save(staff);

        activityLogService.log("STAFF_UPDATED", "Staff", id,
                Map.of("employeeNumber", staff.getEmployeeNumber()));

        return staffMapper.toResponse(staff);
    }
}
