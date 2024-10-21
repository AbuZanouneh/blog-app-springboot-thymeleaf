package com.ats.blogapp.service.interfaces;

import com.ats.blogapp.access.entity.Role;

import java.util.List;
import java.util.Optional;

public interface RoleService {

    List<Role> findAllRoles();

    Optional<Role> findRoleById(Long id);

    Optional<Role> findRoleByName(String name);

    Role saveRole(Role role);

    void deleteRoleById(Long id);
}
