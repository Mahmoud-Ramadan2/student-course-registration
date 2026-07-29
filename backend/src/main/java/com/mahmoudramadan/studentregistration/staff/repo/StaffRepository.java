package com.mahmoudramadan.studentregistration.staff.repo;

import com.mahmoudramadan.studentregistration.staff.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {

    Optional<Staff> findByEmployeeNumber(String employeeNumber);
}
