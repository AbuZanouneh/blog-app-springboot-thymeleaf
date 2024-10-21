package com.ats.blogapp.access.repository;

import com.ats.blogapp.access.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Data Access Layer
public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by username.
    Optional<User> findByUsername(String username);

    // To Check if the username exists or not before update it .... For admin only
    // In ... admin/profile-edit page.
    boolean existsByUsername(String username);

    // Find user by email.
    Optional<User> findByEmail(String email);

    @Override
    @EntityGraph(attributePaths = {"role"})
    Page<User> findAll(Pageable pageable);


}
