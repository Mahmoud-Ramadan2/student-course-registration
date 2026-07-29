package com.mahmoudramadan.studentregistration.user.repository;

import com.mahmoudramadan.studentregistration.user.entity.Role;
import com.mahmoudramadan.studentregistration.user.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {


    Optional<Role> findByRoleName(RoleName roleName);
}
