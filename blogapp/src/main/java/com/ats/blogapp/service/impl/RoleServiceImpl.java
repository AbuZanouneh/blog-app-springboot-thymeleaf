package com.ats.blogapp.service.impl;

import com.ats.blogapp.access.entity.Role;
import com.ats.blogapp.access.repository.RoleRepository;
import com.ats.blogapp.service.interfaces.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public List<Role> findAllRoles(){
        return roleRepository.findAll();
    }

    @Override
    public Optional<Role> findRoleById(Long id){
        return roleRepository.findById(id);
    }

    @Override
    public Optional<Role> findRoleByName(String name){
        return roleRepository.findByName(name);
    }

    @Override
    public Role saveRole(Role role){
        // Business logic can be added here if needed
        return roleRepository.save(role);
    }

    @Override
    public void deleteRoleById(Long id){
        roleRepository.deleteById(id);
    }

}
