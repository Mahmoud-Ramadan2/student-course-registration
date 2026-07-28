package com.mahmoudramadan.studentregistration.user.entity;

import com.mahmoudramadan.studentregistration.shared.entity.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Role extends BaseAuditableEntity {

    @Column(name = "role_name", nullable = false, unique = true, length = 30)
    private String roleName;

    @Column(length = 255)
    private String description;

    @ManyToMany(mappedBy = "roles")
    private Set<User> users = new HashSet<>();

}
