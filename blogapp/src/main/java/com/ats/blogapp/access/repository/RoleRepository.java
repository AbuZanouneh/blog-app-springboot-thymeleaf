package com.ats.blogapp.access.repository;

import com.ats.blogapp.access.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Data Access Layer
public interface RoleRepository extends JpaRepository<Role, Long> {

    // Find Role By Name.
    Optional<Role> findByName(String name);
}
