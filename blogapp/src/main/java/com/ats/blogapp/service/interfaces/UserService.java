package com.ats.blogapp.service.interfaces;

import com.ats.blogapp.access.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserService {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findUserById(Long id);

    boolean existsByUsername(String username);

    User saveUser(User user);

    void deleteUserById(Long id);

    Page<User> findAllUsers(Pageable pageable);

}
